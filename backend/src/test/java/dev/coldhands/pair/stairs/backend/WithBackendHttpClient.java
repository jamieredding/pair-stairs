package dev.coldhands.pair.stairs.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.coldhands.pair.stairs.backend.domain.*;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;

public interface WithBackendHttpClient {

    String BASE_URL = "http://localhost:8080";

    ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    RestTemplate REST_TEMPLATE = new RestTemplate();

    default void createDeveloper(String developerName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("""
                {
                  "name": "%s"
                }""".formatted(developerName), headers);

        ResponseEntity<Void> response = REST_TEMPLATE.postForEntity(STR."\{BASE_URL}/api/v1/developers", request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    default List<Long> getDeveloperIdsFor(List<String> developerNames) throws Exception {
        final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(STR."\{BASE_URL}/api/v1/developers", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        final List<Developer> developers = OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });

        return developers.stream()
                .filter(developer -> developerNames.contains(developer.name()))
                .map(Developer::id)
                .toList();
    }

    default void createStream(String streamName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("""
                {
                  "name": "%s"
                }""".formatted(streamName), headers);

        ResponseEntity<Void> response = REST_TEMPLATE.postForEntity(STR."\{BASE_URL}/api/v1/streams", request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    default List<Long> getStreamIdsFor(List<String> streamNames) throws Exception {
        final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(STR."\{BASE_URL}/api/v1/streams", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        final List<Stream> streams = OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });

        return streams.stream()
                .filter(stream -> streamNames.contains(stream.name()))
                .map(Stream::id)
                .toList();
    }

    default List<ScoredCombination> calculateCombinations(List<Long> developerIds, List<Long> streamIds) throws Exception {
        final CalculateInputDto input = new CalculateInputDto(developerIds, streamIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(OBJECT_MAPPER.writeValueAsString(input), headers);

        ResponseEntity<String> response = REST_TEMPLATE.postForEntity(STR."\{BASE_URL}/api/v1/combinations/calculate", request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        return OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });
    }

    default void saveCombinationEventFor(LocalDate date, List<PairStream> combination) throws Exception {
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

        ResponseEntity<Void> response = REST_TEMPLATE.postForEntity(STR."\{BASE_URL}/api/v1/combinations/event", request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    default List<CombinationEvent> getCombinationEvents() throws Exception {
        ResponseEntity<String> response = REST_TEMPLATE.getForEntity(STR."\{BASE_URL}/api/v1/combinations/event", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        return OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });
    }

    default void deleteCombinationEvent(long id) {
        ResponseEntity<Void> response = REST_TEMPLATE.exchange(STR."\{BASE_URL}/api/v1/combinations/event/{id}", DELETE, null, Void.class, id);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
