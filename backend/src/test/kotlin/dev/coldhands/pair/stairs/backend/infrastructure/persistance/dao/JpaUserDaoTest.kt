package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.transaction.support.TransactionTemplate

@DataJpaTest
open class JpaUserDaoTest @Autowired constructor(
    userRepository: UserRepository,
    val testEntityManager: TestEntityManager,
    val transactionTemplate: TransactionTemplate,
) : UserDaoCdc() {

    override val underTest: UserDao = JpaUserDao(userRepository, dateProvider, precision)

    override fun assertNoUserExistsById(userId: UserId) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.find(UserEntity::class.java, userId.value).shouldBeNull()
        }
    }

    override fun assertNoUserExistsByOidcSub(oidcSub: OidcSub) {
        transactionTemplate.executeWithoutResult {
            testEntityManager.entityManager
                .createQuery("select u from UserEntity u where u.oidcSub = :oidcSub")
                .setParameter("oidcSub", oidcSub.value)
                .resultList.shouldBeEmpty()
        }
    }

    override fun assertUserExists(user: User) {
        transactionTemplate.executeWithoutResult {
            val entity = testEntityManager.find(UserEntity::class.java, user.id.value)
            testEntityManager.detach(entity)
            testEntityManager.find(UserEntity::class.java, user.id.value) should { userEntity ->
                userEntity.id shouldBe user.id.value
                userEntity.oidcSub shouldBe user.oidcSub.value
                userEntity.displayName shouldBe user.displayName
                userEntity.createdAt shouldBe user.createdAt.truncatedTo(precision)
                userEntity.updatedAt shouldBe user.updatedAt.truncatedTo(precision)
            }
        }
    }

    override fun createUser(userDetails: UserDetails): User {
        return transactionTemplate.execute {
            val userEntity = testEntityManager.persistFlushFind(
                UserEntity(
                    oidcSub = userDetails.oidcSub.value,
                    displayName = userDetails.displayName,
                    createdAt = dateProvider.instant(),
                    updatedAt = dateProvider.instant()
                )
            )
            testEntityManager.detach(userEntity)
            userEntity.toDomain()
        }!!
    }
}