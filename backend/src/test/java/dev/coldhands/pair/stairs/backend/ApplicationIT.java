package dev.coldhands.pair.stairs.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.coldhands.pair.stairs.backend.domain.*;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationIT {

    private static final String BASE_URL = "http://localhost:8080";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Test
    @Sql(value = "/delete-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete-test-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void basicFlowStartingFromScratch() throws Exception {
        createDeveloper("dev-0");
        createDeveloper("dev-1");
        createDeveloper("dev-2");
        createDeveloper("dev-3");

        List<Long> developersToIncludeInCombinations = getDeveloperIdsFor(List.of(
                "dev-0",
                "dev-1",
                "dev-2"
        ));

        createStream("stream-a");
        createStream("stream-b");

        List<Long> streamsToIncludeInCombinations = getStreamIdsFor(List.of(
                "stream-a",
                "stream-b"
        ));

        final List<ScoredCombination> scoredCombinations = calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations);

        assertThat(scoredCombinations).isNotEmpty();

        final ScoredCombination bestCombination = scoredCombinations.getFirst();

        assertThat(bestCombination.combination().stream()
                .flatMap(ps -> ps.developers().stream())
                .map(DeveloperInfo::displayName)
                .toList())
                .containsExactlyInAnyOrder("dev-0", "dev-1", "dev-2");

        assertThat(bestCombination.combination().stream()
                .map(PairStream::stream)
                .map(StreamInfo::displayName)
                .toList())
                .containsExactlyInAnyOrder("stream-a", "stream-b");


        saveCombinationEventFor(LocalDate.of(2024, 4, 27), bestCombination.combination());

        final List<CombinationEvent> combinationEvents = getCombinationEvents();

        assertThat(combinationEvents).hasSize(1);

        final CombinationEvent savedEvent = combinationEvents.getFirst();

        assertThat(savedEvent.date()).isEqualTo(LocalDate.of(2024, 4, 27));
        assertThat(savedEvent.combination()).hasSize(2);
        assertThat(savedEvent.combination()).isEqualTo(bestCombination.combination());

        deleteCombinationEvent(savedEvent.id());

        final List<CombinationEvent> combinationEventsAfterDelete = getCombinationEvents();
        assertThat(combinationEventsAfterDelete).isEmpty();
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
