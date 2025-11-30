package dev.coldhands.pair.stairs.backend.domain.developer

import dev.coldhands.pair.stairs.backend.aDeveloperId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
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
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread

@Suppress("unused")
abstract class DeveloperDaoCdc<T : DeveloperDao> {

    abstract val underTest: T

    @Nested
    inner class FindById {

        @Test
        fun `should return null when no developer exists with that id`() {
            val developerId = aDeveloperId()
            assertNoDeveloperExistsWithId(developerId)

            underTest.findById(developerId).shouldBeNull()
        }

        @Test
        fun `should find developer by id when developer exists`() {
            val developerDetails = someDeveloperDetails()
            val developer = createDeveloper(developerDetails)

            underTest.findById(developer.id) shouldBe developer
        }
    }

    @Nested
    inner class FindAllById {

        @Test
        fun `should return empty list when no developers exist`() {
            ensureNoDevelopersExist()

            underTest.findAllById(listOf(DeveloperId(1))).shouldBeEmpty()
        }

        @Test
        fun `should return empty list when no developers match passed ids`() {
            ensureNoDevelopersExist()
            val developer = createDeveloper(someDeveloperDetails())

            underTest.findAllById(listOf(DeveloperId(developer.id.value + 1))).shouldBeEmpty()
        }

        @Test
        fun `should return empty list when no ids are passed in`() {
            ensureNoDevelopersExist()
            createDeveloper(someDeveloperDetails())

            underTest.findAllById(listOf()).shouldBeEmpty()
        }

        @Test
        fun `should find all developers when multiple exist`() {
            ensureNoDevelopersExist()

            val developer1 = createDeveloper(someDeveloperDetails())
            createDeveloper(someDeveloperDetails().copy(name = "another-name"))
            val developer3 = createDeveloper(someDeveloperDetails().copy(name = "yet-another-name"))

            underTest.findAllById(listOf(developer1.id, developer3.id)).shouldContainAll(developer1, developer3)
        }
    }

    @Nested
    inner class FindAll {

        @Test
        fun `should return empty list when no developers exist`() {
            ensureNoDevelopersExist()

            underTest.findAll().shouldBeEmpty()
        }

        @Test
        fun `should find all developers when multiple exist`() {
            ensureNoDevelopersExist()
            val developer1 = createDeveloper(someDeveloperDetails())
            val developer2 = createDeveloper(someDeveloperDetails().copy(name = "another-name"))

            underTest.findAll().shouldContainAll(developer1, developer2)
        }
    }

    @Nested
    inner class Create {

        @Test
        fun `should create developer with specified details and generate id`() {
            val developerDetails = someDeveloperDetails()
            val developer = underTest.create(developerDetails).shouldBeSuccess()

            developer.id.shouldNotBeNull()
            developer.name shouldBe developerDetails.name
            developer.archived shouldBe developerDetails.archived

            assertDeveloperExists(developer)
        }

        @Test
        fun `should create developers with ascending ids`() {
            val developer1 = underTest.create(someDeveloperDetails()).shouldBeSuccess()
            val developer2 = underTest.create(someDeveloperDetails().copy(name = "another-name")).shouldBeSuccess()

            developer1.id shouldNotBe developer2.id
            developer2.id.value shouldBeGreaterThan developer1.id.value
        }

        @Test
        fun `allow max length fields`() {
            val name = "A".repeat(255)

            val developer = underTest.create(
                someDeveloperDetails().copy(
                    name = name,
                )
            ).shouldBeSuccess()

            developer.name shouldBe name
        }

        @Test
        fun `can create developers concurrently`() {
            val ids = ConcurrentLinkedDeque<DeveloperId>()

            (1..10).map { i ->
                thread(name = "many-threads-$i") {
                    repeat(10) {
                        underTest.create(someDeveloperDetails()).shouldBeSuccess {
                            ids.add(it.id)
                        }
                    }
                }
            }.map { it.join() }

            ids.size shouldBe 100
            ids.shouldBeUnique()
            ids.forEach { assertDeveloperExistsWithId(it) }
        }

        @Nested
        inner class SadPath {

            @Test
            fun `name must be 255 characters or less`() {
                val name = "A".repeat(256)

                underTest.create(someDeveloperDetails().copy(name = name))
                    .shouldBeFailure(DeveloperCreateError.NameTooLong(name))
            }
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `can update archived on existing developer`() {
            val developer = givenDeveloperExistsWith(
                archived = false
            )

            val actualDeveloper = underTest.update(
                developerId = developer.id,
                archived = true
            ).shouldBeSuccess()

            actualDeveloper.id shouldBe developer.id
            actualDeveloper.name shouldBe developer.name
            actualDeveloper.archived shouldBe true

            assertDeveloperExists(actualDeveloper)
        }

        @Nested
        inner class SadPath {

            @Test
            fun `return failure when developer does not exist`() {
                val madeUpDeveloperId = aDeveloperId()
                assertNoDeveloperExistsWithId(madeUpDeveloperId)

                underTest.update(developerId = madeUpDeveloperId, archived = true)
                    .shouldBeFailure(DeveloperUpdateError.DeveloperNotFound(madeUpDeveloperId))
            }
        }

        private fun givenDeveloperExistsWith(
            name: String = "some-name",
            archived: Boolean = false
        ): Developer {
            val developerDetails = someDeveloperDetails().copy(
                name = name,
                archived = archived
            )

            val developer = underTest.create(developerDetails).shouldBeSuccess()
            assertDeveloperExists(developer)
            return developer
        }
    }

    abstract fun assertNoDeveloperExistsWithId(developerId: DeveloperId)
    abstract fun assertDeveloperExistsWithId(developerId: DeveloperId)
    abstract fun ensureNoDevelopersExist()
    abstract fun assertDeveloperExists(developer: Developer)
    abstract fun createDeveloper(developerDetails: DeveloperDetails): Developer

    companion object TestFixtures {
        fun someDeveloperDetails(
            name: String = "some-name",
            archived: Boolean = false
        ) = DeveloperDetails(
            name = name,
            archived = archived,
        )
    }
}