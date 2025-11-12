package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.TeamMembershipId
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.coldhands.pair.stairs.backend.domain.team.Team
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembership
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDaoCdc
import dev.coldhands.pair.stairs.backend.domain.user.User
import dev.coldhands.pair.stairs.backend.domain.user.UserDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamMembershipRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository
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
        "spring.datasource.url=jdbc:h2:mem:AJpaTeamMembershipDaoTest;DB_CLOSE_DELAY=-1"
    ]
)
open class JpaTeamMembershipDaoTest @Autowired constructor(
    teamMembershipRepository: TeamMembershipRepository,
    userRepository: UserRepository,
    teamRepository: TeamRepository,
    val testEntityManager: TestEntityManager,
    val transactionTemplate: TransactionTemplate
) : TeamMembershipDaoCdc<JpaTeamMembershipDao>() {
    override val underTest: JpaTeamMembershipDao =
        JpaTeamMembershipDao(teamMembershipRepository, teamRepository, userRepository, dateProvider, precision)

    override fun assertNoTeamExistsWithId(teamId: TeamId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(TeamEntity::class.java, teamId.value).shouldBeNull()
        }
    }

    override fun assertNoUserExistsWithId(userId: UserId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(UserEntity::class.java, userId.value).shouldBeNull()
        }
    }

    override fun assertNoTeamMembershipExistsForId(teamMembershipId: TeamMembershipId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(TeamMembershipEntity::class.java, teamMembershipId.value).shouldBeNull()
        }
    }

    override fun assertNoTeamMembershipExistsForUserId(userId: UserId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("select m from TeamMembershipEntity m where m.user.id = :userId")
                .setParameter("userId", userId.value)
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertNoTeamMembershipExistsForTeamId(teamId: TeamId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager.createQuery("select m from TeamMembershipEntity m where m.team.id = :teamId")
                .setParameter("teamId", teamId.value)
                .resultList
                .shouldBeEmpty()
        }
    }

    override fun assertTeamMembershipExistsWithId(teamMembershipId: TeamMembershipId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(TeamMembershipEntity::class.java, teamMembershipId.value).shouldNotBeNull()
        }
    }

    override fun assertTeamMembershipExists(teamMembership: TeamMembership) {
        transactionTemplate.executeWithoutResult {
            val entity = testEntityManager.find(TeamMembershipEntity::class.java, teamMembership.id.value)
            testEntityManager.detach(entity)
            testEntityManager.find(TeamMembershipEntity::class.java, teamMembership.id.value) should {
                it.id shouldBe teamMembership.id.value
                it.displayName shouldBe teamMembership.displayName
                it.user.toDomain().id shouldBe teamMembership.userId
                it.team.toDomain().id shouldBe teamMembership.teamId
                it.createdAt shouldBe teamMembership.createdAt.truncatedTo(precision)
                it.updatedAt shouldBe teamMembership.updatedAt.truncatedTo(precision)
            }
        }
    }

    override fun createTeam(teamDetails: TeamDetails): Team =
        transactionTemplate.execute {
            val entity = testEntityManager.persistFlushFind(
                TeamEntity(
                    name = teamDetails.name,
                    slug = teamDetails.slug.value,
                    createdAt = dateProvider.instant(),
                    updatedAt = dateProvider.instant()
                )
            )
            testEntityManager.detach(entity)
            entity.toDomain()
        }.shouldNotBeNull()

    override fun createUser(userDetails: UserDetails): User =
        transactionTemplate.execute {
            val entity = testEntityManager.persistFlushFind(
                UserEntity(
                    oidcSub = userDetails.oidcSub.value,
                    displayName = userDetails.displayName,
                    createdAt = dateProvider.instant(),
                    updatedAt = dateProvider.instant()
                )
            )
            testEntityManager.detach(entity)
            entity.toDomain()
        }.shouldNotBeNull()

}