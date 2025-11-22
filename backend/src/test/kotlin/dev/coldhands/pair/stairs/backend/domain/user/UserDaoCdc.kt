package dev.coldhands.pair.stairs.backend.domain.user

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.aUserId
import dev.coldhands.pair.stairs.backend.anOidcSub
import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.UserId
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Suppress("unused")
abstract class UserDaoCdc<T : UserDao> {

    val dateProvider = FakeDateProvider()
    val precision: TemporalUnit = ChronoUnit.MILLIS
    abstract val underTest: T

    @Nested
    inner class FindById {

        @Test
        fun `should return null when no user exists`() {
            val userId = aUserId()
            assertNoUserExistsById(userId)

            underTest.findById(userId).shouldBeNull()
        }

        @Test
        fun `should find user by id when user exists`() {
            val userDetails = someUserDetails()
            val user = createUser(userDetails)

            underTest.findById(user.id) shouldBe user
        }
    }

    @Nested
    inner class FindByOidcSub {

        @Test
        fun `should return null when no user exists`() {
            val oidcSub = anOidcSub()
            assertNoUserExistsByOidcSub(oidcSub)

            underTest.findByOidcSub(oidcSub).shouldBeNull()
        }

        @Test
        fun `should find user by oidc sub when user exists`() {
            val userDetails = someUserDetails()
            val user = createUser(userDetails)

            underTest.findByOidcSub(user.oidcSub) shouldBe user
        }
    }

    @Nested
    inner class Create {

        @Test
        fun `should create user with specified details and generate id`() {
            val userDetails = someUserDetails()
            val now = Instant.now()
            dateProvider.now = now
            val user = underTest.create(userDetails).shouldBeSuccess()

            user.id.shouldNotBeNull()
            user.oidcSub shouldBe userDetails.oidcSub
            user.displayName shouldBe userDetails.displayName
            user.createdAt shouldBe now.truncatedTo(precision)
            user.updatedAt shouldBe now.truncatedTo(precision)

            assertUserExists(user)
        }

        @Test
        fun `should create users with ascending ids`() {
            val user1 = underTest.create(someUserDetails()).shouldBeSuccess()
            val user2 = underTest.create(someUserDetails().copy(oidcSub = anOidcSub())).shouldBeSuccess()

            user1.id shouldNotBe user2.id
            user2.id.value shouldBeGreaterThan user1.id.value
        }

        @Test
        fun `allow max length fields`() {
            val oidcSub = OidcSub("A".repeat(255))
            val displayName = "B".repeat(255)

            val user = underTest.create(
                someUserDetails().copy(
                    oidcSub = oidcSub,
                    displayName = displayName,
                )
            ).shouldBeSuccess()

            user.oidcSub shouldBe oidcSub
            user.displayName shouldBe displayName
        }

        @Test
        fun `can create users concurrently`() {
            val ids = ConcurrentLinkedDeque<UserId>()

            (1..10).map { i ->
                thread(name = "many-threads-$i") {
                    repeat(10) {
                        underTest.create(someUserDetails()).shouldBeSuccess {
                            ids.add(it.id)
                        }
                    }
                }
            }.map { it.join() }

            ids.size shouldBe 100
            ids.shouldBeUnique()
            ids.forEach { underTest.findById(it).shouldNotBeNull() }
        }

        @Nested
        inner class SadPath {

            @Test
            fun `should not allow users with the same oidcSub`() {
                val oidcSub = anOidcSub()
                val user = underTest.create(someUserDetails().copy(oidcSub = oidcSub)).shouldBeSuccess()

                underTest.create(someUserDetails().copy(oidcSub = oidcSub))
                    .shouldBeFailure(UserCreateError.DuplicateOidcSub(oidcSub))

                assertUserExists(user)
            }

            @Test
            fun `oidc sub must be 255 characters or less`() {
                val oidcSub = OidcSub("A".repeat(256))

                underTest.create(someUserDetails().copy(oidcSub = oidcSub))
                    .shouldBeFailure(UserCreateError.OidcSubTooLong(oidcSub))
            }

            @Test
            fun `display name must be 255 characters or less`() {
                val displayName = "A".repeat(256)

                underTest.create(someUserDetails().copy(displayName = displayName))
                    .shouldBeFailure(UserCreateError.DisplayNameTooLong(displayName))
            }
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `can update display name on existing user`() {
            val now = Instant.now()
            dateProvider.now = now
            val user = givenUserExistsWith(
                displayName = "some-display-name"
            )

            val updatedDisplayName = "another-display-name"

            dateProvider.now = now.plus(5.minutes.toJavaDuration())
            val actualUser = underTest.update(user.id, updatedDisplayName).shouldBeSuccess()

            actualUser.id shouldBe user.id
            actualUser.oidcSub shouldBe user.oidcSub
            actualUser.displayName shouldBe updatedDisplayName
            actualUser.createdAt shouldBe now.truncatedTo(precision)
            actualUser.updatedAt shouldBe now.plus(5.minutes.toJavaDuration()).truncatedTo(precision)

            assertUserExists(actualUser)
        }

        @Test
        fun `allow max length fields`() {
            val displayName = "B".repeat(255)

            val user = underTest.create(someUserDetails()).shouldBeSuccess()
            val actualUser = underTest.update(user.id, displayName).shouldBeSuccess()

            actualUser.displayName shouldBe displayName
        }

        @Test
        fun `will update updatedAt even if nothing changed on user`() {
            val now = Instant.now()
            dateProvider.now = now
            val user = givenUserExistsWith(displayName = "some-display-name")

            dateProvider.now = now.plus(5.minutes.toJavaDuration())
            val actualUser = underTest.update(user.id, user.displayName).shouldBeSuccess()

            actualUser.updatedAt shouldBe now.plus(5.minutes.toJavaDuration()).truncatedTo(precision)

            assertUserExists(actualUser)
        }

        @Nested
        inner class SadPath {

            @Test
            fun `return failure when user does not exist`() {
                val madeUpUserId = aUserId()
                assertNoUserExistsById(madeUpUserId)

                underTest.update(madeUpUserId, "irrelevant")
                    .shouldBeFailure(UserUpdateError.UserNotFound(madeUpUserId))
            }

            @Test
            fun `display name must be 255 characters or less`() {
                val displayName = "A".repeat(256)
                val user = givenUserExistsWith()

                underTest.update(user.id, displayName)
                    .shouldBeFailure(UserUpdateError.DisplayNameTooLong(displayName))
            }

        }

        private fun givenUserExistsWith(
            oidcSub: OidcSub = anOidcSub(),
            displayName: String = "some-display-name"
        ): User {
            val userDetails = someUserDetails().copy(
                oidcSub = oidcSub,
                displayName = displayName
            )

            val user = underTest.create(userDetails).shouldBeSuccess()
            assertUserExists(user)
            return user
        }
    }


    abstract fun assertNoUserExistsById(userId: UserId)
    abstract fun assertNoUserExistsByOidcSub(oidcSub: OidcSub)
    abstract fun assertUserExists(user: User)
    abstract fun createUser(userDetails: UserDetails): User

    companion object TestFixtures {
        fun someUserDetails() = UserDetails(
            oidcSub = anOidcSub(),
            displayName = "some-display-name"
        )

        fun someUser() = User(
            id = aUserId(),
            oidcSub = anOidcSub(),
            displayName = "some-display-name",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    }

}