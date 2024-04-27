package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
class BackendCombinationHistoryRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BackendCombinationHistoryRepository underTest;

    @Autowired
    private CombinationEventService combinationEventService;

    @Test
    void getMostRecentCombinationsContainsAllCombinationsInReverseChronologicalOrder() {
        final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
        final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
        final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

        final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
        final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

        combinationEventService.saveEvent(LocalDate.of(2024, 4, 27),
                List.of(
                        new PairStreamByIds(List.of(dev0Id, dev1Id), stream0Id),
                        new PairStreamByIds(List.of(dev2Id), stream1Id)
                )
        );

        combinationEventService.saveEvent(LocalDate.of(2024, 4, 20),
                List.of(
                        new PairStreamByIds(List.of(dev0Id, dev2Id), stream0Id),
                        new PairStreamByIds(List.of(dev1Id), stream1Id)
                )
        );

        final List<Combination<PairStream>> mostRecentCombinations = underTest.getMostRecentCombinations(2);

        assertThat(mostRecentCombinations).containsExactly(
                new Combination<>(Set.of(
                        new PairStream(Set.of(dev0Id.toString(), dev1Id.toString()), stream0Id.toString()),
                        new PairStream(Set.of(dev2Id.toString()), stream1Id.toString())
                )),
                new Combination<>(Set.of(
                        new PairStream(Set.of(dev0Id.toString(), dev2Id.toString()), stream0Id.toString()),
                        new PairStream(Set.of(dev1Id.toString()), stream1Id.toString())
                ))
        );
    }

    @Test
    void getMostRecentCombinationsOnlyIncludeCountNumberOfResults() {
        final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
        final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
        final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

        final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
        final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

        combinationEventService.saveEvent(LocalDate.of(2024, 4, 27),
                List.of(
                        new PairStreamByIds(List.of(dev0Id, dev1Id), stream0Id),
                        new PairStreamByIds(List.of(dev2Id), stream1Id)
                )
        );

        combinationEventService.saveEvent(LocalDate.of(2024, 4, 20),
                List.of(
                        new PairStreamByIds(List.of(dev0Id, dev2Id), stream0Id),
                        new PairStreamByIds(List.of(dev1Id), stream1Id)
                )
        );

        final List<Combination<PairStream>> mostRecentCombinations = underTest.getMostRecentCombinations(1);

        assertThat(mostRecentCombinations).containsExactly(
                new Combination<>(Set.of(
                        new PairStream(Set.of(dev0Id.toString(), dev1Id.toString()), stream0Id.toString()),
                        new PairStream(Set.of(dev2Id.toString()), stream1Id.toString())
                ))
        );
    }
}