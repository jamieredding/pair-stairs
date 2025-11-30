package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDaoCdc
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.*
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.support.TransactionTemplate

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

    override fun createStream(streamId: StreamDetails): Stream =
        streamDao.create(streamId).shouldBeSuccess()

}