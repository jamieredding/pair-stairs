package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coldhands.pair.stairs.backend.domain.Developer;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

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
public class DeveloperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestEntityManager testEntityManager;

    @Nested
    class Read {

        @Test
        void whenNoDevelopersThenReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/developers"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void whenMultipleDevelopersThenReturnThem() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();

            mockMvc.perform(get("/api/v1/developers"))
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
                            ]""".formatted(dev0Id, "dev-0", dev1Id, "dev-1")))
            ;
        }
    }

    @Nested
    class ReadDeveloperInfo {

        @Test
        void whenNoDevelopersThenReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/developers/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void whenMultipleDevelopersThenReturnThem() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();

            mockMvc.perform(get("/api/v1/developers/info"))
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
                            ]""".formatted(dev0Id, "dev-0", dev1Id, "dev-1")))
            ;
        }
    }

    @Nested
    class Write {

        @Test
        void saveADeveloper() throws Exception {
            final MvcResult result = mockMvc.perform(post("/api/v1/developers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "dev-0"
                                    }""")
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("dev-0"))
                    .andReturn();

            final Developer developer = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Developer.class);
            final Long actualId = developer.id();

            final DeveloperEntity savedDeveloper = testEntityManager.find(DeveloperEntity.class, actualId);

            assertThat(savedDeveloper.getId(), equalTo(actualId));
            assertThat(savedDeveloper.getName(), equalTo("dev-0"));
        }

    }

    @Nested
    class ReadDeveloperStats {

        @Autowired
        private CombinationEventService combinationEventService;

        @Test
        void whenDeveloperDoesNotExistWithIdThenReturnNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/developers/1/stats"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(""));
        }

        @Test
        void whenDeveloperExistsButNoPairsHaveHappenedTheReturnJustThemselves() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id))
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
                              ],
                              "streamStats": []
                            }""".formatted(dev0Id)));
        }

        @Test
        void whenDeveloperExistsAndHasPairedThenReturnStatistics() throws Exception {
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

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "developerStats": [
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
                                },
                                {
                                  "developer": {
                                    "id": %s,
                                    "displayName": "dev-0"
                                  },
                                  "count": 0
                                }
                              ],
                              "streamStats": [
                                {
                                  "stream": {
                                    "id": %s,
                                    "displayName": "stream-a"
                                  },
                                  "count": 3
                                },
                                {
                                  "stream": {
                                    "id": %s,
                                    "displayName": "stream-b"
                                  },
                                  "count": 0
                                }
                              ]
                            }""".formatted(dev1Id, dev2Id, dev3Id, dev0Id, streamAId, streamBId)));
        }

        @Test
        void whenDeveloperExistsAndOtherDevelopersAndStreamsButNoEventsHaveHappenedThenSortAlphabetically() throws Exception {
            final Long dev3Id = testEntityManager.persist(new DeveloperEntity("dev-3")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();

            final Long streamCId = testEntityManager.persist(new StreamEntity("stream-c")).getId();
            final Long streamBId = testEntityManager.persist(new StreamEntity("stream-b")).getId();
            final Long streamAId = testEntityManager.persist(new StreamEntity("stream-a")).getId();

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id))
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
                              ],
                              "streamStats": [
                                {
                                  "stream": {
                                    "id": %s,
                                    "displayName": "stream-a"
                                  },
                                  "count": 0
                                },
                                {
                                  "stream": {
                                    "id": %s,
                                    "displayName": "stream-b"
                                  },
                                  "count": 0
                                },
                                {
                                  "stream": {
                                    "id": %s,
                                    "displayName": "stream-c"
                                  },
                                  "count": 0
                                }
                              ]
                            }""".formatted(dev1Id, dev2Id, dev3Id, dev0Id, streamAId, streamBId, streamCId)));
        }

        /*
        todo
            - date range search
         */
    }
}
