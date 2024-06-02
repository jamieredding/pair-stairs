package dev.coldhands.pair.stairs.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.coldhands.pair.stairs.backend.domain.*;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Only for manually running, 1.4M size in h2")
public class DatabaseSizeTestIT {

    private static final String BASE_URL = "http://localhost:18081";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Test
    void runSimulationToFillTheDatabase() throws Exception {
        LocalDate currentDay = LocalDate.now();
        final int yearsToSimulate = 30;
        final long daysToSimulate = ChronoUnit.DAYS.between(currentDay, currentDay.plusYears(yearsToSimulate));

        System.out.println(STR."Simulating \{yearsToSimulate} years (\{daysToSimulate} days)");

        for (int dayNumber = 0; dayNumber < daysToSimulate; dayNumber++, currentDay = currentDay.plusDays(1)) {
            simulateChoiceForDay(currentDay);
        }
    }

    private void simulateChoiceForDay(LocalDate currentDay) throws Exception {
        final List<Long> developersToIncludeInCombinations = getDeveloperIdsFor(List.of(
                "dev-0",
                "dev-1",
                "dev-2",
                "dev-3",
                "dev-4"
        ));

        final List<Long> streamsToIncludeInCombinations = getStreamIdsFor(List.of(
                "stream-a",
                "stream-b",
                "stream-c"
        ));

        final List<ScoredCombination> scoredCombinations = calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations);

        assertThat(scoredCombinations).isNotEmpty();

        final ScoredCombination bestCombination = scoredCombinations.getFirst();

        saveCombinationEventFor(currentDay, bestCombination.combination());

        final List<CombinationEvent> combinationEvents = getCombinationEvents();

        final CombinationEvent savedEvent = combinationEvents.getFirst();

        assertThat(savedEvent.date()).isEqualTo(currentDay);
    }

    @Nested
    class Actuator {

        @Test
        void health() {
            final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(BASE_URL + "/actuator/health", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            final String responseBody = response.getBody();
            final DocumentContext parsed = JsonPath.parse(responseBody);

            assertThat(parsed.read("$.status", String.class)).isEqualTo("UP");
            assertThat(parsed.read("$.components.db.status", String.class)).isEqualTo("UP");
            assertThat(parsed.read("$.components.db.details.database", String.class)).isEqualTo("MySQL");
            assertThat(parsed.read("$.components.livenessState.status", String.class)).isEqualTo("UP");
            assertThat(parsed.read("$.components.readinessState.status", String.class)).isEqualTo("UP");
        }

        @Test
        void info() {
            final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(BASE_URL + "/actuator/info", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            final String responseBody = response.getBody();
            final DocumentContext parsed = JsonPath.parse(responseBody);

            assertThat(parsed.read("$.build.version", String.class)).isNotNull();

            assertThat(parsed.read("$.git.branch", String.class)).isNotNull();
            assertThat(parsed.read("$.git.commit.id", String.class)).isNotNull();
        }
    }

    private void createDeveloper(String developerName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("""
                {
                  "name": "%s"
                }""".formatted(developerName), headers);

        ResponseEntity<Void> response = REST_TEMPLATE.postForEntity(BASE_URL + "/api/v1/developers", request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private List<Long> getDeveloperIdsFor(List<String> developerNames) throws Exception {
        final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(BASE_URL + "/api/v1/developers", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        final List<Developer> developers = OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });

        return developers.stream()
                .filter(developer -> developerNames.contains(developer.name()))
                .map(Developer::id)
                .toList();
    }

    private void createStream(String streamName) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("""
                {
                  "name": "%s"
                }""".formatted(streamName), headers);

        ResponseEntity<Void> response = REST_TEMPLATE.postForEntity(BASE_URL + "/api/v1/streams", request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private List<Long> getStreamIdsFor(List<String> streamNames) throws Exception {
        final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(BASE_URL + "/api/v1/streams", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        final List<Stream> streams = OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });

        return streams.stream()
                .filter(stream -> streamNames.contains(stream.name()))
                .map(Stream::id)
                .toList();
    }

    private List<ScoredCombination> calculateCombinations(List<Long> developerIds, List<Long> streamIds) throws Exception {
        final CalculateInputDto input = new CalculateInputDto(developerIds, streamIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(OBJECT_MAPPER.writeValueAsString(input), headers);

        ResponseEntity<String> response = REST_TEMPLATE.postForEntity(BASE_URL + "/api/v1/combinations/calculate", request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        return OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });
    }

    private void saveCombinationEventFor(LocalDate date, List<PairStream> combination) throws Exception {
        List<SaveCombinationEventDto.PairStreamByIds> combinationByIds = combination.stream()
                .map(ps -> {
                    List<Long> developerIds = ps.developers().stream()
                            .map(DeveloperInfo::id)
                            .toList();
                    return new SaveCombinationEventDto.PairStreamByIds(developerIds, ps.stream().id());
                })
                .toList();

        final SaveCombinationEventDto dto = new SaveCombinationEventDto(date, combinationByIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(OBJECT_MAPPER.writeValueAsString(dto), headers);

        ResponseEntity<Void> response = REST_TEMPLATE.postForEntity(BASE_URL + "/api/v1/combinations/event", request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private List<CombinationEvent> getCombinationEvents() throws Exception {
        ResponseEntity<String> response = REST_TEMPLATE.getForEntity(BASE_URL + "/api/v1/combinations/event", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        return OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });
    }

    private void deleteCombinationEvent(long id) {
        ResponseEntity<Void> response = REST_TEMPLATE.exchange(BASE_URL + "/api/v1/combinations/event/{id}", HttpMethod.DELETE, null, Void.class, id);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
