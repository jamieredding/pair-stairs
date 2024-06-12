package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coldhands.pair.stairs.backend.domain.Stream;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public class StreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestEntityManager testEntityManager;

    @Nested
    class Read {

        @Test
        void whenNoStreamThenReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/streams"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void whenMultipleStreamsThenReturnThem() throws Exception {
            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-0")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-1")).getId();

            mockMvc.perform(get("/api/v1/streams"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            [
                                {
                                    "id": %s,
                                    "name": "%s"
                                },
                                {
                                    "id": %s,
                                    "name": "%s"
                                }
                            ]""".formatted(stream0Id, "stream-0", stream1Id, "stream-1")))

            ;
        }
    }

    @Nested
    class ReadStreamInfo {

        @Test
        void whenNoStreamThenReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/streams/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void whenMultipleStreamsThenReturnThem() throws Exception {
            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-0")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-1")).getId();

            mockMvc.perform(get("/api/v1/streams/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            [
                                {
                                    "id": %s,
                                    "displayName": "%s"
                                },
                                {
                                    "id": %s,
                                    "displayName": "%s"
                                }
                            ]""".formatted(stream0Id, "stream-0", stream1Id, "stream-1")))

            ;
        }
    }


    @Nested
    class Write {

        @Test
        void saveAStream() throws Exception {
            final MvcResult result = mockMvc.perform(post("/api/v1/streams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "stream-0"
                                    }""")
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("stream-0"))
                    .andReturn();

            final Stream stream = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Stream.class);
            final Long actualId = stream.id();

            final StreamEntity savedStream = testEntityManager.find(StreamEntity.class, actualId);

            assertThat(savedStream.getId(), equalTo(actualId));
            assertThat(savedStream.getName(), equalTo("stream-0"));
        }

    }

    @Nested
    class ReadStreamStats {

        @Autowired
        private CombinationEventService combinationEventService;

        @Test
        void whenStreamDoesNotExistWithIdThenReturnNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/streams/1/stats"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(""));
        }

        @Test
        void whenStreamExistsButNoPairsHaveHappenedThenReturnAllDevelopers() throws Exception {
            final Long streamAId = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-0"
                                  },
                                  "count": 0
                                }
                              ]
                            }""".formatted(dev0Id)));
        }

        @Test
        void whenStreamExistsAndHasBeenPairedOnThenReturnStatistics() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();
            final Long dev3Id = testEntityManager.persist(new DeveloperEntity("dev-3")).getId();

            final Long streamAId = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long streamBId = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            combinationEventService.saveEvent(LocalDate.of(2024, 5, 5), List.of(
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev0Id, dev1Id), streamAId),
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev2Id), streamBId)
            ));
            combinationEventService.saveEvent(LocalDate.of(2024, 5, 6), List.of(
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev0Id, dev2Id), streamAId),
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev1Id), streamBId)
            ));
            combinationEventService.saveEvent(LocalDate.of(2024, 5, 7), List.of(
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev0Id, dev1Id), streamAId),
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev2Id), streamBId)
            ));

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-0"
                                  },
                                  "count": 3
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-1"
                                  },
                                  "count": 2
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-2"
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-3"
                                  },
                                  "count": 0
                                }
                              ]
                            }""".formatted(dev0Id, dev1Id, dev2Id, dev3Id)));
        }

        @Test
        void whenStreamExistsAndDevelopersButNoEventsHaveHappenedThenSortAlphabetically() throws Exception {
            final Long dev3Id = testEntityManager.persist(new DeveloperEntity("dev-3")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();

            final Long streamAId = testEntityManager.persist(new StreamEntity("stream-a")).getId();

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-1"
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-2"
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-3"
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-0"
                                  },
                                  "count": 0
                                }
                              ]
                            }""".formatted(dev1Id, dev2Id, dev3Id, dev0Id)));
        }

        @Test
        void allowsFilteringByDateRange() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();
            final Long dev3Id = testEntityManager.persist(new DeveloperEntity("dev-3")).getId();

            final Long streamAId = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long streamBId = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            combinationEventService.saveEvent(LocalDate.of(2024, 5, 5), List.of(
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev0Id, dev1Id), streamAId),
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev2Id), streamBId)
            ));
            combinationEventService.saveEvent(LocalDate.of(2024, 5, 6), List.of(
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev0Id, dev2Id), streamAId),
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev1Id), streamBId)
            ));
            combinationEventService.saveEvent(LocalDate.of(2024, 5, 7), List.of(
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev0Id, dev1Id), streamAId),
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev2Id), streamBId)
            ));
            combinationEventService.saveEvent(LocalDate.of(2024, 5, 8), List.of(
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev0Id, dev2Id), streamAId),
                    new SaveCombinationEventDto.PairStreamByIds(List.of(dev1Id), streamBId)
            ));

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId)
                            .queryParam("startDate", "2024-05-06")
                            .queryParam("endDate", "2024-05-07"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-0"
                                  },
                                  "count": 2
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-1"
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-2"
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-3"
                                  },
                                  "count": 0
                                }
                              ]
                            }""".formatted(dev0Id, dev1Id, dev2Id, dev3Id)));
        }

        static java.util.stream.Stream<Arguments> badRequestIfInvalidRange() {
            return java.util.stream.Stream.of(
                    Arguments.of(
                            "startDate only",
                            (Function<MockHttpServletRequestBuilder, MockHttpServletRequestBuilder>) builder ->
                                    builder.queryParam("startDate", "2024-05-06")),
                    Arguments.of(
                            "endDate only",
                            (Function<MockHttpServletRequestBuilder, MockHttpServletRequestBuilder>) builder ->
                                    builder.queryParam("endDate", "2024-05-06")),
                    Arguments.of(
                            "startDate after endDate",
                            (Function<MockHttpServletRequestBuilder, MockHttpServletRequestBuilder>) builder ->
                                    builder
                                            .queryParam("startDate", "2024-05-07")
                                            .queryParam("endDate", "2024-05-06"))
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource
        void badRequestIfInvalidRange(String testName, Function<MockHttpServletRequestBuilder, MockHttpServletRequestBuilder> builder) throws Exception {
            mockMvc.perform(builder.apply(get("/api/v1/streams/1/stats")))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(""));
        }
    }
}
