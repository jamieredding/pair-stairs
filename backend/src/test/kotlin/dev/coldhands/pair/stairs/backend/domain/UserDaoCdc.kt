package dev.coldhands.pair.stairs.backend.domain

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.aUserId
import dev.coldhands.pair.stairs.backend.anOidcSub
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Suppress("unused")
abstract class UserDaoCdc {

    val dateProvider = FakeDateProvider()
    val precision: TemporalUnit = ChronoUnit.MILLIS
    abstract val underTest: UserDao

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
            fun `oidc sub must be less than 255 characters`() {
                val oidcSub = OidcSub("A".repeat(256))

                underTest.create(someUserDetails().copy(oidcSub = oidcSub))
                    .shouldBeFailure(UserCreateError.OidcSubTooLong(oidcSub))
            }

            @Test
            fun `display name must be less than 255 characters`() {
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

            val updatedUserBeforePersist = user.copy(
                displayName = "another-display-name"
            )

            dateProvider.now = now.plus(5.minutes.toJavaDuration())
            val actualUser = underTest.update(updatedUserBeforePersist).shouldBeSuccess()

            actualUser.id shouldBe user.id
            actualUser.oidcSub shouldBe user.oidcSub
            actualUser.displayName shouldBe updatedUserBeforePersist.displayName
            actualUser.createdAt shouldBe now.truncatedTo(precision)
            actualUser.updatedAt shouldBe now.plus(5.minutes.toJavaDuration()).truncatedTo(precision)

            assertUserExists(actualUser)
        }

        @Test
        fun `will update updatedAt even if nothing changed on user`() {
            val now = Instant.now()
            dateProvider.now = now
            val user = givenUserExistsWith(displayName = "some-display-name")

            dateProvider.now = now.plus(5.minutes.toJavaDuration())
            val actualUser = underTest.update(user).shouldBeSuccess()

            actualUser.updatedAt shouldBe now.plus(5.minutes.toJavaDuration()).truncatedTo(precision)

            assertUserExists(actualUser)
        }

        @Nested
        inner class SadPath {

            @Test
            fun `return failure when user does not exist`() {
                val madeUpUserId = aUserId()
                assertNoUserExistsById(madeUpUserId)

                val madeUpUser = someUser().copy(id = madeUpUserId)

                underTest.update(madeUpUser)
                    .shouldBeFailure(UserUpdateError.UserNotFound(madeUpUserId))
            }

            @Test
            fun `do not allow updating of oidc sub as there is no requirement for this yet`() {
                val user = givenUserExistsWith(oidcSub = anOidcSub()).copy(
                    oidcSub = anOidcSub()
                )

                underTest.update(user)
                    .shouldBeFailure(UserUpdateError.CannotChangeOidcSub)
            }

            @Test
            fun `do not allow updating of created at as there is no requirement for this yet`() {
                val user = givenUserExistsWith().copy(
                    createdAt = Instant.now()
                )

                underTest.update(user)
                    .shouldBeFailure(UserUpdateError.CannotChangeCreatedAt)
            }

            @Test
            fun `do not allow updating of updated at as there is no requirement for this yet`() {
                val user = givenUserExistsWith().copy(
                    updatedAt = Instant.now()
                )

                underTest.update(user)
                    .shouldBeFailure(UserUpdateError.CannotChangeUpdatedAt)
            }

            @Test
            fun `display name must be less than 255 characters`() {
                val displayName = "A".repeat(256)
                val user = givenUserExistsWith().copy(displayName = displayName)

                underTest.update(user)
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