package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.anOidcSub
import dev.coldhands.pair.stairs.backend.domain.RealDateProvider
import dev.coldhands.pair.stairs.backend.domain.User
import dev.coldhands.pair.stairs.backend.domain.UserName
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeUserDao
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.temporal.ChronoUnit

class UserDetailsServiceTest {

    private val userDao = FakeUserDao(RealDateProvider(), ChronoUnit.MILLIS)
    private val underTest: UserDetailsService = UserDetailsService(userDao)

    @Nested
    inner class CreateOrUpdate {

        @Test
        fun `create user details when user does not exist`() {
            val userName = UserName(
                nickName = null,
                givenName = null,
                fullName = "Jamie Redding",
            )
            val inputOidcSub = anOidcSub()

            val actualUser: User = underTest.createOrUpdate(inputOidcSub, userName).shouldBeSuccess()

            userDao.findById(actualUser.id).shouldNotBeNull {
                id shouldBe actualUser.id
                oidcSub shouldBe inputOidcSub
                displayName shouldBe "Jamie"
            }
        }

        @Test
        fun `update user details when user does exist`() {
            val inputOidcSub = anOidcSub()

            val initialUser = underTest.createOrUpdate(
                inputOidcSub,
                UserName(nickName = null, givenName = null, fullName = "Jamie Redding")
            ).shouldBeSuccess()

            val updatedUser = underTest.createOrUpdate(
                inputOidcSub,
                UserName(
                    nickName = "Jay",
                    givenName = null,
                    fullName = "Jamie Redding",
                )
            ).shouldBeSuccess()

            initialUser.id shouldBe updatedUser.id

            userDao.findById(initialUser.id).shouldNotBeNull {
                id shouldBe initialUser.id
                oidcSub shouldBe inputOidcSub
                displayName shouldBe "Jay"
            }
        }
    }
}