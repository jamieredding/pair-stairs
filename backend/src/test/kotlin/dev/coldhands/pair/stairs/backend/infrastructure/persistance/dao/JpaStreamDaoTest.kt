package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDaoCdc
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository
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
        "spring.datasource.url=jdbc:h2:mem:JpaStreamDaoTest;DB_CLOSE_DELAY=-1"
    ]
)
open class JpaStreamDaoTest @Autowired constructor(
    streamRepository: StreamRepository,
    val testEntityManager: TestEntityManager,
    val transactionTemplate: TransactionTemplate
) : StreamDaoCdc<JpaStreamDao>() {
    override val underTest: JpaStreamDao = JpaStreamDao(streamRepository)

    override fun assertNoStreamExistsWithId(streamId: StreamId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(StreamEntity::class.java, streamId.value).shouldBeNull()
        }
    }

    override fun assertStreamExistsWithId(streamId: StreamId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(StreamEntity::class.java, streamId.value).shouldNotBeNull()
        }
    }

    override fun ensureNoStreamsExist() {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("select d from StreamEntity d")
                .resultList
                .map {
                    testEntityManager.remove(it)
                    testEntityManager.flush()
                }

            testEntityManager.entityManager.createQuery("select d from StreamEntity d")
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertStreamExists(stream: Stream) {
        transactionTemplate.executeWithoutResult {
            val entity = testEntityManager.find(StreamEntity::class.java, stream.id.value)
            testEntityManager.detach(entity)
            testEntityManager.find(StreamEntity::class.java, stream.id.value) should { streamEntity ->
                streamEntity.id shouldBe stream.id.value
                streamEntity.name shouldBe stream.name
                streamEntity.archived shouldBe stream.archived
            }
        }
    }

    override fun createStream(streamDetails: StreamDetails): Stream =
        transactionTemplate.execute {
            val streamEntity = testEntityManager.persistFlushFind(
                StreamEntity(
                    name = streamDetails.name,
                    archived = streamDetails.archived,
                )
            )
            testEntityManager.detach(streamEntity)
            streamEntity.toDomain()
        }.shouldNotBeNull()
}