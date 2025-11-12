package dev.coldhands.pair.stairs.backend.domain.team.membership

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.aTeamId
import dev.coldhands.pair.stairs.backend.aTeamMembershipId
import dev.coldhands.pair.stairs.backend.aUserId
import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.domain.TeamDaoCdc.TestFixtures.someTeamDetails
import dev.coldhands.pair.stairs.backend.domain.UserDaoCdc.TestFixtures.someUserDetails
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread

@Suppress("unused")
abstract class TeamMembershipDaoCdc<T : TeamMembershipDao> {

    val dateProvider = FakeDateProvider()
    val precision: TemporalUnit = ChronoUnit.MILLIS
    abstract val underTest: T

    @Nested
    inner class FindById {

        @Test
        fun `should return null when no team membership exists`() {
            val teamMembershipId = aTeamMembershipId()
            assertNoTeamMembershipExistsForId(teamMembershipId)

            underTest.findById(teamMembershipId).shouldBeNull()
        }

        @Test
        fun `should find user by id when user exists`() {
            val user = createUser(someUserDetails())
            val team = createTeam(someTeamDetails())

            val teamMembershipDetails = someTeamMembershipDetails(
                userId = user.id,
                teamId = team.id,
            )

            val teamMembership = underTest.create(teamMembershipDetails).shouldBeSuccess()

            underTest.findById(teamMembership.id) shouldBe teamMembership
        }
    }


    @Nested
    inner class Create {

        @Test
        fun `should create team membership with specified details and generate id`() {
            val user = createUser(someUserDetails())
            val team = createTeam(someTeamDetails())

            val teamMembershipDetails = someTeamMembershipDetails(
                userId = user.id,
                teamId = team.id,
            )

            val now = Instant.now()
            dateProvider.now = now
            val teamMembership = underTest.create(teamMembershipDetails).shouldBeSuccess()

            teamMembership.id.shouldNotBeNull()
            teamMembership.displayName shouldBe teamMembershipDetails.displayName
            teamMembership.userId shouldBe teamMembershipDetails.userId
            teamMembership.teamId shouldBe teamMembershipDetails.teamId
            teamMembership.createdAt shouldBe now.truncatedTo(precision)
            teamMembership.updatedAt shouldBe now.truncatedTo(precision)

            assertTeamMembershipExists(teamMembership)
        }

        @Test
        fun `should create team memberships with ascending ids`() {
            val user1 = createUser(someUserDetails())
            val user2 = createUser(someUserDetails())
            val team = createTeam(someTeamDetails())

            val teamMembership1 = underTest.create(
                someTeamMembershipDetails(
                    userId = user1.id,
                    teamId = team.id,
                )
            ).shouldBeSuccess()
            val teamMembership2 = underTest.create(someTeamMembershipDetails(
                userId = user2.id,
                teamId = team.id,
            )).shouldBeSuccess()

            teamMembership1.id shouldNotBe teamMembership2.id
            teamMembership2.id.value shouldBeGreaterThan teamMembership1.id.value
        }

        @Test
        fun `allow max length fields`() {
            val user = createUser(someUserDetails())
            val team = createTeam(someTeamDetails())

            val displayName = "A".repeat(255)
            val teamMembershipDetails = someTeamMembershipDetails(
                displayName = displayName,
                userId = user.id,
                teamId = team.id,
            )

            val teamMembership = underTest.create(teamMembershipDetails).shouldBeSuccess()

            teamMembership.displayName shouldBe displayName
        }

        @Test
        fun `can create teams memberships concurrently`() {
            val ids = ConcurrentLinkedDeque<TeamMembershipId>()
            val team = createTeam(someTeamDetails())

            (1..10).map { i ->
                thread(name = "many-threads-$i") {
                    repeat(10) {
                        val user = createUser(someUserDetails())
                        val teamMembershipDetails = someTeamMembershipDetails(
                            userId = user.id,
                            teamId = team.id,
                        )

                        underTest.create(teamMembershipDetails).shouldBeSuccess {
                            ids.add(it.id)
                        }
                    }
                }
            }.map { it.join() }

            ids.size shouldBe 100
            ids.shouldBeUnique()
            ids.forEach { assertTeamMembershipExistsWithId(it) }
        }

        @Nested
        inner class SadPath {

            @Test
            fun `do not create team membership if team does not exist`() {
                val user = createUser(someUserDetails())
                val teamMembershipDetails = someTeamMembershipDetails(
                    userId = user.id,
                    teamId = aTeamId(),
                )

                assertNoTeamExistsWithId(teamMembershipDetails.teamId)

                underTest.create(teamMembershipDetails).shouldBeFailure()

                assertNoTeamMembershipExistsForUserId(teamMembershipDetails.userId)
            }

            @Test
            fun `do not create team membership if user does not exist`() {
                val team = createTeam(someTeamDetails())
                val teamMembershipDetails = someTeamMembershipDetails(
                    userId = aUserId(),
                    teamId = team.id,
                )

                assertNoUserExistsWithId(teamMembershipDetails.userId)

                underTest.create(teamMembershipDetails).shouldBeFailure()

                assertNoTeamMembershipExistsForTeamId(teamMembershipDetails.teamId)
            }

            @Test
            fun `display name must be 255 characters or less`() {
                val user = createUser(someUserDetails())
                val team = createTeam(someTeamDetails())

                val displayName = "A".repeat(256)
                val teamMembershipDetails = someTeamMembershipDetails(
                    displayName = displayName,
                    userId = user.id,
                    teamId = team.id,
                )

                underTest.create(teamMembershipDetails)
                    .shouldBeFailure()

                assertNoTeamMembershipExistsForUserId(teamMembershipDetails.userId)
            }
        }
    }

    abstract fun assertNoTeamExistsWithId(teamId: TeamId)
    abstract fun assertNoUserExistsWithId(userId: UserId)
    abstract fun assertNoTeamMembershipExistsForId(teamMembershipId: TeamMembershipId)
    abstract fun assertNoTeamMembershipExistsForUserId(userId: UserId)
    abstract fun assertNoTeamMembershipExistsForTeamId(teamId: TeamId)
    abstract fun assertTeamMembershipExistsWithId(teamMembershipId: TeamMembershipId)
    abstract fun assertTeamMembershipExists(teamMembership: TeamMembership)
    abstract fun createTeam(teamDetails: TeamDetails): Team
    abstract fun createUser(userDetails: UserDetails): User

    companion object TestFixtures {
        fun someTeamMembershipDetails(
            displayName: String = "some-display-name",
            userId: UserId = aUserId(),
            teamId: TeamId = aTeamId(),
        ) = TeamMembershipDetails(
            displayName = displayName,
            userId = userId,
            teamId = teamId,
        )
    }
}