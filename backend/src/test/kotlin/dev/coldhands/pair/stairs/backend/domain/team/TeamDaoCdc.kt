package dev.coldhands.pair.stairs.backend.domain.team

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.aSlug
import dev.coldhands.pair.stairs.backend.aTeamId
import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.collections.shouldContainAll
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
abstract class TeamDaoCdc<T : TeamDao> {

    val dateProvider = FakeDateProvider()
    val precision: TemporalUnit = ChronoUnit.MILLIS
    abstract val underTest: T

    @Nested
    inner class FindById {

        @Test
        fun `should return null when no team exists with that id`() {
            val teamId = aTeamId()
            assertNoTeamExistsWithId(teamId)

            underTest.findById(teamId).shouldBeNull()
        }

        @Test
        fun `should find team by slug when team exists`() {
            val teamDetails = someTeamDetails()
            val team = createTeam(teamDetails)

            underTest.findById(team.id) shouldBe team
        }
    }

    @Nested
    inner class FindBySlug {

        @Test
        fun `should return null when no team exists with that slug`() {
            val slug = aSlug()
            assertNoTeamExistsWithSlug(slug)

            underTest.findBySlug(slug).shouldBeNull()
        }

        @Test
        fun `should find team by slug when team exists`() {
            val teamDetails = someTeamDetails()
            val team = createTeam(teamDetails)

            underTest.findBySlug(team.slug) shouldBe team
        }
    }

    @Nested
    inner class FindAll {

        @Test
        fun `should return empty list when no teams exist`() {
            ensureNoTeamsExist()

            underTest.findAll().shouldBeEmpty()
        }

        @Test
        fun `should find all teams when multiple exist`() {
            val team1 = createTeam(someTeamDetails())
            val team2 = createTeam(someTeamDetails().copy(slug = aSlug()))

            underTest.findAll().shouldContainAll(team1, team2)
        }
    }


    @Nested
    inner class Create {

        @Test
        fun `should create team with specified details and generate id`() {
            val teamDetails = someTeamDetails()
            val now = Instant.now()
            dateProvider.now = now
            val team = underTest.create(teamDetails).shouldBeSuccess()

            team.id.shouldNotBeNull()
            team.name shouldBe teamDetails.name
            team.slug shouldBe teamDetails.slug
            team.createdAt shouldBe now.truncatedTo(precision)
            team.updatedAt shouldBe now.truncatedTo(precision)

            assertTeamExists(team)
        }

        @Test
        fun `should create teams with ascending ids`() {
            val team1 = underTest.create(someTeamDetails()).shouldBeSuccess()
            val team2 = underTest.create(someTeamDetails().copy(slug = aSlug())).shouldBeSuccess()

            team1.id shouldNotBe team2.id
            team2.id.value shouldBeGreaterThan team1.id.value
        }

        @Test
        fun `allow max length fields`() {
            val slug = Slug("A".repeat(255))
            val name = "B".repeat(255)

            val team = underTest.create(
                someTeamDetails().copy(
                    slug = slug,
                    name = name,
                )
            ).shouldBeSuccess()

            team.slug shouldBe slug
            team.name shouldBe name
        }

        @Test
        fun `can create teams concurrently`() {
            val ids = ConcurrentLinkedDeque<TeamId>()

            (1..10).map { i ->
                thread(name = "many-threads-$i") {
                    repeat(10) {
                        underTest.create(someTeamDetails()).shouldBeSuccess {
                            ids.add(it.id)
                        }
                    }
                }
            }.map { it.join() }

            ids.size shouldBe 100
            ids.shouldBeUnique()
            ids.forEach { assertTeamExistsWithId(it) }
        }

        @Nested
        inner class SadPath {

            @Test
            fun `should not allow teams with the same slug`() {
                val slug = aSlug()
                val team = underTest.create(someTeamDetails().copy(slug = slug)).shouldBeSuccess()

                underTest.create(someTeamDetails().copy(slug = slug))
                    .shouldBeFailure(TeamCreateError.DuplicateSlug(slug))

                assertTeamExists(team)
            }

            @Test
            fun `slug must be 255 characters or less`() {
                val slug = Slug("A".repeat(256))

                underTest.create(someTeamDetails().copy(slug = slug))
                    .shouldBeFailure(TeamCreateError.SlugTooLong(slug))
            }

            @Test
            fun `name must be 255 characters or less`() {
                val name = "A".repeat(256)

                underTest.create(someTeamDetails().copy(name = name))
                    .shouldBeFailure(TeamCreateError.NameTooLong(name))
            }
        }
    }

    abstract fun assertNoTeamExistsWithId(teamId: TeamId)
    abstract fun assertTeamExistsWithId(teamId: TeamId)
    abstract fun ensureNoTeamsExist()
    abstract fun assertNoTeamExistsWithSlug(slug: Slug)
    abstract fun assertTeamExists(team: Team)
    abstract fun createTeam(teamDetails: TeamDetails): Team

    companion object TestFixtures {
        fun someTeamDetails() = TeamDetails(
            slug = aSlug(),
            name = "some-name"
        )

        fun someTeam() = Team(
            id = aTeamId(),
            name = "some-name",
            slug = aSlug(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}