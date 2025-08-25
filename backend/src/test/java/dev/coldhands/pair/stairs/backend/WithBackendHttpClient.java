package dev.coldhands.pair.stairs.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.coldhands.pair.stairs.backend.domain.*;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;

public interface WithBackendHttpClient {

    String BASE_URL = "http://localhost:8080";
    String IDP_TOKEN_URL = "http://localhost:5556/dex/token";
    String OAUTH_CLIENT_ID = "pair-stairs";
    String OAUTH_CLIENT_SECRET = "ZXhhbXBsZS1hcHAtc2VjcmV0";
    String USERNAME = "admin@example.com";
    String PASSWORD = "password";

    ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    RestTemplate REST_TEMPLATE = initialiseRestTemplate();

    private static RestTemplate initialiseRestTemplate() {
        final var restTemplate = new RestTemplate();
        final String jwt = fetchJwt(restTemplate);

        restTemplate.getInterceptors().add(((request, body, execution) -> {
            request.getHeaders().setBearerAuth(jwt);
            return execution.execute(request, body);
        }));

        return restTemplate;
    }

    default long createDeveloper(String developerName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("""
                {
                  "name": "%s"
                }""".formatted(developerName), headers);

        ResponseEntity<Developer> response = REST_TEMPLATE.postForEntity(STR."\{BASE_URL}/api/v1/developers", request, Developer.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final Developer developer = response.getBody();
        return developer.id();
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

    default long createStream(String streamName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("""
                {
                  "name": "%s"
                }""".formatted(streamName), headers);

        ResponseEntity<Stream> response = REST_TEMPLATE.postForEntity(STR."\{BASE_URL}/api/v1/streams", request, Stream.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final Stream stream = response.getBody();
        return stream.id();
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

    default DeveloperStats getDeveloperStatsBetween(long developerId, LocalDate startDate, LocalDate endDate) throws Exception {
        final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(STR."\{BASE_URL}/api/v1/developers/\{developerId}/stats?startDate=\{startDate.format(ISO_DATE)}&endDate=\{endDate.format(ISO_DATE)}", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        return OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });
    }

    default StreamStats getStreamStatsBetween(long streamId, LocalDate startDate, LocalDate endDate) throws Exception {
        final ResponseEntity<String> response = REST_TEMPLATE.getForEntity(STR."\{BASE_URL}/api/v1/streams/\{streamId}/stats?startDate=\{startDate.format(ISO_DATE)}&endDate=\{endDate.format(ISO_DATE)}", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final String responseBody = response.getBody();

        return OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
        });
    }

    record TokenResponse(String access_token) {
    }

    private static String fetchJwt(RestTemplate restTemplate) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("username", USERNAME);
        form.add("password", PASSWORD);
        form.add("scope", "openid profile email"); // add offline_access if needed

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET);

        var response = restTemplate.postForEntity(IDP_TOKEN_URL, new HttpEntity<>(form, headers), TokenResponse.class);

        return response.getBody().access_token();
    }

}
