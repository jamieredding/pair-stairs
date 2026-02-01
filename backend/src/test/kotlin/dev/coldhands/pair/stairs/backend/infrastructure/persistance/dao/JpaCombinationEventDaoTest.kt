package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDaoCdc
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDetails
import dev.coldhands.pair.stairs.backend.domain.combination.PairStream
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDaoCdc.TestFixtures.someDeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDaoCdc.TestFixtures.someStreamDetails
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.*
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread

@DataJpaTest
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:JpaCombinationEventDaoTest;DB_CLOSE_DELAY=-1"
    ]
)
open class JpaCombinationEventDaoTest @Autowired constructor(
    developerRepository: DeveloperRepository,
    streamRepository: StreamRepository,
    combinationEventRepository: CombinationEventRepository,
    combinationRepository: CombinationRepository,
    pairStreamRepository: PairStreamRepository,
    val testEntityManager: TestEntityManager,
    val transactionTemplate: TransactionTemplate,
) : CombinationEventDaoCdc<JpaCombinationEventDao>() {
    private val developerDao = JpaDeveloperDao(developerRepository)
    private val streamDao = JpaStreamDao(streamRepository)
    override val underTest: JpaCombinationEventDao = JpaCombinationEventDao(
        combinationEventRepository,
        combinationRepository,
        pairStreamRepository,
        developerDao,
        streamDao
    )

    @Nested
    open inner class JpaOnly {

        @Test
        fun `creating events with identical combination reuses same combination row`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val dev2 = createDeveloper(someDeveloperDetails(name = "dev-2"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val combination = setOf(
                PairStream(setOf(dev0.id, dev1.id), stream0.id),
                PairStream(setOf(dev2.id), stream1.id),
            )

            val combinationEvent1 =
                underTest.create(CombinationEventDetails(date = now, combination = combination)).shouldBeSuccess()
            val combinationEvent2 =
                underTest.create(CombinationEventDetails(date = now.plusDays(1), combination = combination))
                    .shouldBeSuccess()

            transactionTemplate.executeWithoutResult {
                // 1) Only one combination row for this domain combination
                countPersistedCombinations(combination) shouldBe 1

                // 2) Both events point at the same combination_id
                val eventEntity1 =
                    testEntityManager.find(CombinationEventEntity::class.java, combinationEvent1.id.value)
                val eventEntity2 =
                    testEntityManager.find(CombinationEventEntity::class.java, combinationEvent2.id.value)

                eventEntity1.combination.id shouldBe eventEntity2.combination.id
            }
        }

        @Test
        fun `creating events reuses existing pairstream rows`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))

            val shared = PairStream(setOf(dev0.id, dev1.id), stream0.id)

            countPersistedPairStreams(shared) shouldBe 0
            countPersistedCombinations(setOf(shared)) shouldBe 0

            underTest.create(CombinationEventDetails(now, setOf(shared))).shouldBeSuccess()
            underTest.create(CombinationEventDetails(now.plusDays(1), setOf(shared))).shouldBeSuccess()

            transactionTemplate.executeWithoutResult {
                countPersistedPairStreams(shared) shouldBe 1
                countPersistedCombinations(setOf(shared)) shouldBe 1
            }
        }

        @Test
        fun `shared pairstream is reused across different combinations`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val dev2 = createDeveloper(someDeveloperDetails(name = "dev-2"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))
            val stream1 = createStream(someStreamDetails(name = "stream-1"))

            val shared = PairStream(setOf(dev0.id, dev1.id), stream0.id)
            val other1 = PairStream(setOf(dev2.id), stream1.id)
            val other2 = PairStream(setOf(dev2.id), stream0.id) // just an example distinct PairStream

            val combA = setOf(shared, other1)
            val combB = setOf(shared, other2)

            underTest.create(CombinationEventDetails(now, combA)).shouldBeSuccess()
            underTest.create(CombinationEventDetails(now.plusDays(1), combB)).shouldBeSuccess()

            transactionTemplate.executeWithoutResult {
                countPersistedPairStreams(shared) shouldBe 1

                // Two different combinations should exist (unless your rules consider them the same)
                countPersistedCombinations(combA) shouldBe 1
                countPersistedCombinations(combB) shouldBe 1
            }
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        // Ideally this wouldn't allow duplicates but until this is an issue, we don't need to worry about supporting
        // it and migrating existing data to ensure we only have existing data with unique rows
        open fun `concurrent creates will allow duplicate combination or pairstream rows`() {
            val dev0 = createDeveloper(someDeveloperDetails(name = "dev-0"))
            val dev1 = createDeveloper(someDeveloperDetails(name = "dev-1"))
            val stream0 = createStream(someStreamDetails(name = "stream-0"))

            val shared = PairStream(setOf(dev0.id, dev1.id), stream0.id)
            val comb = setOf(shared)

            val ids = ConcurrentLinkedDeque<CombinationEventId>()

            (1..10).map { i ->
                thread(name = "dedupe-$i") {
                    repeat(10) { j ->
                        underTest.create(
                            CombinationEventDetails(
                                date = now.plusDays(i.toLong()).plusYears(j.toLong()),
                                combination = comb
                            )
                        ).shouldBeSuccess { ids.add(it.id) }
                    }
                }
            }.forEach { it.join() }

            ids.size shouldBe 100
            ids.shouldBeUnique()

            // The deduped invariants:
            // This can be uncommented if we ever truly fix the uniqueness issue
//            countPersistedPairStreams(shared) shouldBe 1
//            countPersistedCombinations(comb) shouldBe 1
        }

    }

    override fun assertNoCombinationEventExistsWithId(combinationEventId: CombinationEventId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(CombinationEventEntity::class.java, combinationEventId.value).shouldBeNull()
        }
    }

    override fun assertNoCombinationEventExistsWithId(developerId: DeveloperId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("SELECT c FROM CombinationEventEntity c JOIN c.combination.pairs p JOIN p.developers d WHERE d.id = :developerId")
                .setParameter("developerId", developerId.value)
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertNoCombinationEventExistsWithId(streamId: StreamId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("SELECT c FROM CombinationEventEntity c JOIN c.combination.pairs p WHERE p.stream.id = :streamId")
                .setParameter("streamId", streamId.value)
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertNoDeveloperExistsWithId(developerId: DeveloperId) {
        developerDao.findById(developerId).shouldBeNull()
    }

    override fun assertNoStreamExistsWithId(streamId: StreamId) {
        streamDao.findById(streamId).shouldBeNull()
    }

    override fun assertCombinationEventExistsWithId(combinationEventId: CombinationEventId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(CombinationEventEntity::class.java, combinationEventId.value).shouldNotBeNull()
        }
    }

    override fun ensureNoCombinationEventsExist() {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("select e from CombinationEventEntity e")
                .resultList
                .map {
                    testEntityManager.remove(it)
                    testEntityManager.flush()
                }

            testEntityManager.entityManager.createQuery("select e from CombinationEventEntity e")
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertCombinationEventExists(combinationEvent: CombinationEvent) {
        transactionTemplate.executeWithoutResult {
            val entity = testEntityManager.find(CombinationEventEntity::class.java, combinationEvent.id.value)
            testEntityManager.detach(entity)
            testEntityManager.find(CombinationEventEntity::class.java, combinationEvent.id.value) should {
                it.id shouldBe combinationEvent.id.value
                it.date shouldBe combinationEvent.date
                it.combination.toDomain() shouldBe combinationEvent.combination
            }
        }

    }

    override fun createDeveloper(developerDetails: DeveloperDetails): Developer =
        developerDao.create(developerDetails).shouldBeSuccess()

    override fun createStream(streamDetails: StreamDetails): Stream =
        streamDao.create(streamDetails).shouldBeSuccess()

    private fun findAllPairStreamEntities(): List<PairStreamEntity> =
        testEntityManager.entityManager
            .createQuery("SELECT ps FROM PairStreamEntity ps", PairStreamEntity::class.java)
            .resultList

    private fun findAllCombinationEntities(): List<CombinationEntity> =
        testEntityManager.entityManager
            .createQuery("SELECT c FROM CombinationEntity c", CombinationEntity::class.java)
            .resultList

    private fun countPersistedPairStreams(expected: PairStream): Int =
        findAllPairStreamEntities().count { it.toDomain() == expected }

    private fun countPersistedCombinations(expected: Set<PairStream>): Int =
        findAllCombinationEntities().count { it.toDomain() == expected }


}