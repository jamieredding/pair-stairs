package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.team.Team
import dev.coldhands.pair.stairs.backend.domain.team.TeamDaoCdc
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository
import io.kotest.matchers.collections.shouldBeEmpty
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
        "spring.datasource.url=jdbc:h2:mem:JpaTeamDaoTest;DB_CLOSE_DELAY=-1"
    ]
)
open class JpaTeamDaoTest @Autowired constructor(
    teamRepository: TeamRepository,
    val testEntityManager: TestEntityManager,
    val transactionTemplate: TransactionTemplate
) : TeamDaoCdc<JpaTeamDao>() {
    override val underTest: JpaTeamDao = JpaTeamDao(teamRepository, dateProvider, precision)

    override fun assertTeamExistsWithId(teamId: TeamId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(TeamEntity::class.java, teamId.value).shouldNotBeNull()
        }
    }

    override fun ensureNoTeamsExist() {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("select t from TeamEntity t")
                .resultList
                .map {
                    testEntityManager.remove(it)
                    testEntityManager.flush()
                }

            testEntityManager.entityManager.createQuery("select t from TeamEntity t")
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertNoTeamExistsWithSlug(slug: Slug) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("select t from TeamEntity t where t.slug = :slug")
                .setParameter("slug", slug.value)
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertTeamExists(team: Team) {
        transactionTemplate.executeWithoutResult {
            val entity = testEntityManager.find(TeamEntity::class.java, team.id.value)
            testEntityManager.detach(entity)
            testEntityManager.find(TeamEntity::class.java, team.id.value) should { teamEntity ->
                teamEntity.id shouldBe team.id.value
                teamEntity.slug shouldBe team.slug.value
                teamEntity.name shouldBe team.name
                teamEntity.createdAt shouldBe team.createdAt.truncatedTo(precision)
                teamEntity.updatedAt shouldBe team.updatedAt.truncatedTo(precision)
            }
        }
    }

    override fun createTeam(teamDetails: TeamDetails): Team =
        transactionTemplate.execute {
            val teamEntity = testEntityManager.persistFlushFind(
                TeamEntity(
                    slug = teamDetails.slug.value,
                    name = teamDetails.name,
                    createdAt = dateProvider.instant(),
                    updatedAt = dateProvider.instant()
                )
            )
            testEntityManager.detach(teamEntity)
            teamEntity.toDomain()
        }.shouldNotBeNull()

}