package dev.coldhands.pair.stairs.backend.domain

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

@Suppress("unused")
abstract class UserDaoCdc {

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
            val user = underTest.create(userDetails).shouldBeSuccess()

            user.id.shouldNotBeNull()
            user.oidcSub shouldBe userDetails.oidcSub
            user.displayName shouldBe userDetails.displayName

            assertUserExists(user)
        }

        @Test
        fun `should create users with ascending ids`() {
            val user1 = underTest.create(someUserDetails()).shouldBeSuccess()
            val user2 = underTest.create(someUserDetails().copy(oidcSub = anOidcSub())).shouldBeSuccess()

            user1.id shouldNotBe user2.id
            user2.id.value shouldBeGreaterThan user1.id.value
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
            val user = givenUserExistsWith(
                displayName = "some-display-name"
            )

            val updatedUserBeforePersist = user.copy(
                displayName = "another-display-name"
            )

            val actualUser = underTest.update(updatedUserBeforePersist).shouldBeSuccess()

            actualUser.id shouldBe user.id
            actualUser.oidcSub shouldBe user.oidcSub
            actualUser.displayName shouldBe updatedUserBeforePersist.displayName

            assertUserExists(actualUser)
        }

        @Nested
        inner class SadPath {

            @Test
            fun `return failure when user does not exist`() {
                val madeUpUserId = aUserId()
                assertNoUserExistsById(madeUpUserId)

                val madeUpUser = User(id = madeUpUserId, oidcSub = anOidcSub(), displayName = "some-display-name")

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

    }

}