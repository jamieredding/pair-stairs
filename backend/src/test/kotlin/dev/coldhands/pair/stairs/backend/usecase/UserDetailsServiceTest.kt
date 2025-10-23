package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.UserName
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
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
import java.util.*
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
            val inputOidcSub = UUID.randomUUID().toString()

            val user: DomainUser = underTest.createOrUpdate(inputOidcSub, userName)

            val userEntity = testEntityManager.find(UserEntity::class.java, user.id)

            userEntity.shouldNotBeNull {
                id shouldBe user.id
                oidcSub shouldBe inputOidcSub
                displayName shouldBe "Jamie"
                createdAt.shouldBe(Instant.now() plusOrMinus 1.seconds)
                updatedAt.shouldBe(Instant.now() plusOrMinus 1.seconds)
            }
        }

        @Test
        fun `update user details when user does exist`() {
            val inputOidcSub = UUID.randomUUID().toString()

            val user = underTest.createOrUpdate(
                inputOidcSub,
                UserName(nickName = null, givenName = null, fullName = "Jamie Redding")
            )
            val initial = testEntityManager.find(UserEntity::class.java, user.id)
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

            val userEntity = testEntityManager.find(UserEntity::class.java, user.id)

            userEntity.shouldNotBeNull {
                id shouldBe user.id
                oidcSub shouldBe inputOidcSub
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
            underTest.getUserByOidcSub(UUID.randomUUID().toString()).shouldBeNull()
        }

        @Test
        fun `get user by oidcSub when user exists`() {
            val oidcSub = UUID.randomUUID().toString()

            val createdUser = underTest.createOrUpdate(oidcSub, UserName(null, null, "User"))

            underTest.getUserByOidcSub(oidcSub) shouldBe createdUser
        }
    }
}