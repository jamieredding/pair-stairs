package dev.coldhands.pair.stairs.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coldhands.pair.stairs.backend.domain.*;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractAcceptanceTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
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

        final List<CombinationEvent> startingEvents = findAllCombinationEvents();

        assertThat(startingEvents).isEmpty();

        saveCombinationEventFor(LocalDate.of(2024, 4, 27), bestCombination.combination());

        final List<CombinationEvent> allCombinationEvents = findAllCombinationEvents();

        assertThat(allCombinationEvents).hasSize(1);

        final CombinationEvent savedEvent = allCombinationEvents.getFirst();

        assertThat(savedEvent.date()).isEqualTo(LocalDate.of(2024, 4, 27));
        assertThat(savedEvent.combination()).hasSize(2);
        assertThat(savedEvent.combination()).isEqualTo(bestCombination.combination());
    }

    @Test
    void exampleOfSecondDay() throws Exception {
        basicFlowStartingFromScratch();

        final CombinationEvent yesterdayEvent = findAllCombinationEvents().getFirst();

        List<Long> developersToIncludeInCombinations = getDeveloperIdsFor(List.of(
                "dev-0",
                "dev-1",
                "dev-2"
        ));

        List<Long> streamsToIncludeInCombinations = getStreamIdsFor(List.of(
                "stream-a",
                "stream-b"
        ));

        final List<ScoredCombination> scoredCombinations = calculateCombinations(developersToIncludeInCombinations, streamsToIncludeInCombinations);

        assertThat(scoredCombinations).isNotEmpty();

        final List<PairStream> todayCombination = scoredCombinations.getFirst().combination();

        final List<PairStream> yesterdayCombination = yesterdayEvent.combination();

        assertThat(todayCombination)
                .isNotEqualTo(yesterdayCombination);


        saveCombinationEventFor(LocalDate.of(2024, 4, 28), todayCombination);

        final List<CombinationEvent> allCombinationEvents = findAllCombinationEvents();

        assertThat(allCombinationEvents).hasSize(2);

        final CombinationEvent savedEvent = allCombinationEvents.getFirst();

        assertThat(savedEvent.date()).isEqualTo(LocalDate.of(2024, 4, 28));
        assertThat(savedEvent.combination()).hasSize(2);

        deleteCombinationEvent(savedEvent.id());

        final List<CombinationEvent> allCombinationEventsAfterDelete = findAllCombinationEvents();

        assertThat(allCombinationEventsAfterDelete).hasSize(1);
        assertThat(allCombinationEventsAfterDelete).containsExactly(yesterdayEvent);
    }

    private void createDeveloper(String developerName) throws Exception {
        mockMvc.perform(post("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "%s"
                        }""".formatted(developerName))

        ).andExpect(status().isCreated());
    }

    private List<Long> getDeveloperIdsFor(List<String> developerNames) throws Exception {
        final MvcResult result = mockMvc.perform(get("/api/v1/developers"))
                .andExpect(status().isOk())
                .andReturn();
        final String responseBody = result.getResponse()
                .getContentAsString();

        final List<Developer> developers = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        return developers.stream()
                .filter(developer -> developerNames.contains(developer.name()))
                .map(Developer::id)
                .toList();
    }

    private void createStream(String streamName) throws Exception {
        mockMvc.perform(post("/api/v1/streams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }""".formatted(streamName))
                )
                .andExpect(status().isCreated());
    }

    private List<Long> getStreamIdsFor(List<String> streamNames) throws Exception {
        final String responseBody = mockMvc.perform(get("/api/v1/streams"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final List<Stream> streams = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        return streams.stream()
                .filter(stream -> streamNames.contains(stream.name()))
                .map(Stream::id)
                .toList();
    }

    private List<ScoredCombination> calculateCombinations(List<Long> developerIds, List<Long> streamIds) throws Exception {
        final CalculateInputDto input = new CalculateInputDto(developerIds, streamIds);

        final String responseBody = mockMvc.perform(post("/api/v1/combinations/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseBody, new TypeReference<>() {
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


        mockMvc.perform(post("/api/v1/combinations/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    private List<CombinationEvent> findAllCombinationEvents() throws Exception {
        final String responseBody = mockMvc.perform(get("/api/v1/combinations/event"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();
        return objectMapper.readValue(responseBody, new TypeReference<>() {
        });
    }

    private void deleteCombinationEvent(long id) throws Exception {
        mockMvc.perform(delete("/api/v1/combinations/event/{id}", id))
                .andExpect(status().isNoContent());
    }
}
