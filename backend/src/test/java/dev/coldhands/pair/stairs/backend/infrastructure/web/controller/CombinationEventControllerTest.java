package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
class CombinationEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestEntityManager testEntityManager;

    @Nested
    class Write {

        @Test
        void saveACombination() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            mockMvc.perform(post("/api/v1/combinations/event")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "date": "2024-04-27",
                                      "combination": [
                                        {
                                          "developerIds": [%s, %s],
                                          "streamId": %s
                                        },
                                        {
                                          "developerIds": [%s],
                                          "streamId": %s
                                        }
                                      ]
                                    }""".formatted(dev0Id, dev1Id, stream0Id, dev2Id, stream1Id))
                    )
                    .andExpect(status().isCreated());

            final CombinationEventEntity savedCombinationEvent = testEntityManager.getEntityManager()
                    .createQuery("SELECT c FROM CombinationEventEntity c WHERE c.date = :date", CombinationEventEntity.class)
                    .setParameter("date", LocalDate.parse("2024-04-27"))
                    .getSingleResult();

            assertThat(savedCombinationEvent.getDate()).isEqualTo(LocalDate.of(2024, 4, 27));

            final List<PairStreamEntity> pairs = savedCombinationEvent.getCombination().getPairs();

            final List<PairStream> pairStreams = toSimpleDomain(pairs);

            assertThat(pairStreams).containsExactly(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            );
        }

    }

    private static List<PairStream> toSimpleDomain(List<PairStreamEntity> pairs) {
        return pairs.stream()
                .map(ps -> {
                    final Set<String> developers = ps.getDevelopers().stream()
                            .map(DeveloperEntity::getName)
                            .collect(Collectors.toSet());
                    return new PairStream(developers, ps.getStream().getName());
                })
                .toList();
    }
}