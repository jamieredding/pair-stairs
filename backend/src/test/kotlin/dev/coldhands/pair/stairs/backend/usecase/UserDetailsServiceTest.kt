package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.anOidcSub
import dev.coldhands.pair.stairs.backend.domain.UserName
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.date.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import dev.coldhands.pair.stairs.backend.domain.User as DomainUser

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class UserDetailsServiceTest @Autowired constructor(
    private val underTest: UserDetailsService,
    private val testEntityManager: TestEntityManager,
) {

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

            val user: DomainUser = underTest.createOrUpdate(inputOidcSub, userName).shouldBeSuccess()

            val userEntity = testEntityManager.find(UserEntity::class.java, user.id.value)

            userEntity.shouldNotBeNull {
                id shouldBe user.id.value
                oidcSub shouldBe inputOidcSub.value
                displayName shouldBe "Jamie"
                createdAt.shouldBe(Instant.now() plusOrMinus 1.seconds)
                updatedAt.shouldBe(Instant.now() plusOrMinus 1.seconds)
            }
        }

        @Test
        fun `update user details when user does exist`() {
            val inputOidcSub = anOidcSub()

            val user = underTest.createOrUpdate(
                inputOidcSub,
                UserName(nickName = null, givenName = null, fullName = "Jamie Redding")
            ).shouldBeSuccess()
            val initial = testEntityManager.find(UserEntity::class.java, user.id.value)
            val initialCreatedAt = initial.createdAt
            val initialUpdatedAt = initial.updatedAt!!

            underTest.createOrUpdate(
                inputOidcSub,
                UserName(
                    nickName = "Jay",
                    givenName = null,
                    fullName = "Jamie Redding",
                )
            )

            val userEntity = testEntityManager.find(UserEntity::class.java, user.id.value)

            userEntity.shouldNotBeNull {
                id shouldBe user.id.value
                oidcSub shouldBe inputOidcSub.value
                displayName shouldBe "Jay"
                createdAt shouldBe initialCreatedAt
                updatedAt.shouldNotBeNull() shouldBeGreaterThan initialUpdatedAt
            }
        }
    }

    @Nested
    inner class GetUserByOidcSub {

        @Test
        fun `get user by oidcSub when user does not exist`() {
            underTest.getUserByOidcSub(anOidcSub()).shouldBeNull()
        }

        @Test
        fun `get user by oidcSub when user exists`() {
            val oidcSub = anOidcSub()

            val createdUser = underTest.createOrUpdate(oidcSub, UserName(null, null, "User"))
                .shouldBeSuccess()

            underTest.getUserByOidcSub(oidcSub) shouldBe createdUser
        }
    }
}