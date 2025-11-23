package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.aStreamDetails
import dev.coldhands.pair.stairs.backend.asString
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds
import dev.coldhands.pair.stairs.core.domain.Combination
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactly
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class BackendCombinationHistoryRepositoryTest @Autowired constructor(
    private val developerDao: DeveloperDao,
    private val streamDao: StreamDao,
    private val underTest: BackendCombinationHistoryRepository,
    private val combinationEventService: CombinationEventService,
) {

    @Test
    fun `getMostRecentCombinations contains all combinations in reverse chronological order`() {
        val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
        val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
        val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

        val stream0Id = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
        val stream1Id = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

        combinationEventService.saveEvent(
            LocalDate.of(2024, 4, 27),
            listOf(
                PairStreamByIds(listOf(dev0Id, dev1Id), stream0Id),
                PairStreamByIds(listOf(dev2Id), stream1Id),
            ),
        )

        combinationEventService.saveEvent(
            LocalDate.of(2024, 4, 20),
            listOf(
                PairStreamByIds(listOf(dev0Id, dev2Id), stream0Id),
                PairStreamByIds(listOf(dev1Id), stream1Id),
            ),
        )

        val mostRecentCombinations = underTest.getMostRecentCombinations(2)

        mostRecentCombinations shouldContainExactly listOf(
            Combination(
                setOf(
                    PairStream(setOf(dev0Id.asString(), dev1Id.asString()), stream0Id.asString()),
                    PairStream(setOf(dev2Id.asString()), stream1Id.asString()),
                ),
            ),
            Combination(
                setOf(
                    PairStream(setOf(dev0Id.asString(), dev2Id.asString()), stream0Id.asString()),
                    PairStream(setOf(dev1Id.asString()), stream1Id.asString()),
                ),
            ),
        )
    }

    @Test
    fun `getMostRecentCombinations only includes requested number of results`() {
        val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
        val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
        val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

        val stream0Id = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
        val stream1Id = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

        combinationEventService.saveEvent(
            LocalDate.of(2024, 4, 27),
            listOf(
                PairStreamByIds(listOf(dev0Id, dev1Id), stream0Id),
                PairStreamByIds(listOf(dev2Id), stream1Id),
            ),
        )

        combinationEventService.saveEvent(
            LocalDate.of(2024, 4, 20),
            listOf(
                PairStreamByIds(listOf(dev0Id, dev2Id), stream0Id),
                PairStreamByIds(listOf(dev1Id), stream1Id),
            ),
        )

        val mostRecentCombinations = underTest.getMostRecentCombinations(1)

        mostRecentCombinations shouldContainExactly listOf(
            Combination(
                setOf(
                    PairStream(setOf(dev0Id.asString(), dev1Id.asString()), stream0Id.asString()),
                    PairStream(setOf(dev2Id.asString()), stream1Id.asString()),
                ),
            ),
        )
    }
}
