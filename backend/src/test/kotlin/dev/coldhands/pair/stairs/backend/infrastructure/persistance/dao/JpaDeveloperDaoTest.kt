package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDaoCdc
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository
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
        "spring.datasource.url=jdbc:h2:mem:JpaDeveloperDaoTest;DB_CLOSE_DELAY=-1"
    ]
)
open class JpaDeveloperDaoTest @Autowired constructor(
    developerRepository: DeveloperRepository,
    val testEntityManager: TestEntityManager,
    val transactionTemplate: TransactionTemplate
) : DeveloperDaoCdc<JpaDeveloperDao>() {
    override val underTest: JpaDeveloperDao = JpaDeveloperDao(developerRepository)

    override fun assertNoDeveloperExistsWithId(developerId: DeveloperId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(DeveloperEntity::class.java, developerId.value).shouldBeNull()
        }
    }

    override fun assertDeveloperExistsWithId(developerId: DeveloperId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(DeveloperEntity::class.java, developerId.value).shouldNotBeNull()
        }
    }

    override fun ensureNoDevelopersExist() {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("select d from DeveloperEntity d")
                .resultList
                .map {
                    testEntityManager.remove(it)
                    testEntityManager.flush()
                }

            testEntityManager.entityManager.createQuery("select d from DeveloperEntity d")
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertDeveloperExists(developer: Developer) {
        transactionTemplate.executeWithoutResult {
            val entity = testEntityManager.find(DeveloperEntity::class.java, developer.id.value)
            testEntityManager.detach(entity)
            testEntityManager.find(DeveloperEntity::class.java, developer.id.value) should { developerEntity ->
                developerEntity.id shouldBe developer.id.value
                developerEntity.name shouldBe developer.name
                developerEntity.archived shouldBe developer.archived
            }
        }
    }

    override fun createDeveloper(developerDetails: DeveloperDetails): Developer =
        transactionTemplate.execute {
            val developerEntity = testEntityManager.persistFlushFind(
                DeveloperEntity(
                    name = developerDetails.name,
                    archived = developerDetails.archived,
                )
            )
            testEntityManager.detach(developerEntity)
            developerEntity.toDomain()
        }.shouldNotBeNull()
}