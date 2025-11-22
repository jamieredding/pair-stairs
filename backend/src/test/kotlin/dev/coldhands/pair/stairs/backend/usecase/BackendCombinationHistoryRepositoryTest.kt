package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds
import dev.coldhands.pair.stairs.backend.toDeveloperIds
import dev.coldhands.pair.stairs.core.domain.Combination
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream
import io.kotest.matchers.collections.shouldContainExactly
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class BackendCombinationHistoryRepositoryTest @Autowired constructor(
    private val testEntityManager: TestEntityManager,
    private val underTest: BackendCombinationHistoryRepository,
    private val combinationEventService: CombinationEventService,
) {

    @Test
    fun `getMostRecentCombinations contains all combinations in reverse chronological order`() {
        val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
        val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
        val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

        val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
        val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

        combinationEventService.saveEvent(
            LocalDate.of(2024, 4, 27),
            listOf(
                PairStreamByIds(listOf(dev0Id, dev1Id).toDeveloperIds(), stream0Id),
                PairStreamByIds(listOf(dev2Id).toDeveloperIds(), stream1Id),
            ),
        )

        combinationEventService.saveEvent(
            LocalDate.of(2024, 4, 20),
            listOf(
                PairStreamByIds(listOf(dev0Id, dev2Id).toDeveloperIds(), stream0Id),
                PairStreamByIds(listOf(dev1Id).toDeveloperIds(), stream1Id),
            ),
        )

        val mostRecentCombinations = underTest.getMostRecentCombinations(2)

        mostRecentCombinations shouldContainExactly listOf(
            Combination(
                setOf(
                    PairStream(setOf(dev0Id.toString(), dev1Id.toString()), stream0Id.toString()),
                    PairStream(setOf(dev2Id.toString()), stream1Id.toString()),
                ),
            ),
            Combination(
                setOf(
                    PairStream(setOf(dev0Id.toString(), dev2Id.toString()), stream0Id.toString()),
                    PairStream(setOf(dev1Id.toString()), stream1Id.toString()),
                ),
            ),
        )
    }

    @Test
    fun `getMostRecentCombinations only includes requested number of results`() {
        val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
        val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
        val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

        val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
        val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

        combinationEventService.saveEvent(
            LocalDate.of(2024, 4, 27),
            listOf(
                PairStreamByIds(listOf(dev0Id, dev1Id).toDeveloperIds(), stream0Id),
                PairStreamByIds(listOf(dev2Id).toDeveloperIds(), stream1Id),
            ),
        )

        combinationEventService.saveEvent(
            LocalDate.of(2024, 4, 20),
            listOf(
                PairStreamByIds(listOf(dev0Id, dev2Id).toDeveloperIds(), stream0Id),
                PairStreamByIds(listOf(dev1Id).toDeveloperIds(), stream1Id),
            ),
        )

        val mostRecentCombinations = underTest.getMostRecentCombinations(1)

        mostRecentCombinations shouldContainExactly listOf(
            Combination(
                setOf(
                    PairStream(setOf(dev0Id.toString(), dev1Id.toString()), stream0Id.toString()),
                    PairStream(setOf(dev2Id.toString()), stream1Id.toString()),
                ),
            ),
        )
    }
}
