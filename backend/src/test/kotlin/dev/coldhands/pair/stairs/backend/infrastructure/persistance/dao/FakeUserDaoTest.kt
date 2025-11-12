package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.coldhands.pair.stairs.backend.domain.user.User
import dev.coldhands.pair.stairs.backend.domain.user.UserDaoCdc
import dev.coldhands.pair.stairs.backend.domain.user.UserDetails
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class FakeUserDaoTest: UserDaoCdc<FakeUserDao>() {
    override val underTest = FakeUserDao(dateProvider, precision)

    override fun assertNoUserExistsById(userId: UserId) {
        underTest.usersView[userId].shouldBeNull()
    }

    override fun assertNoUserExistsByOidcSub(oidcSub: OidcSub) {
        underTest.usersView.values.map { it.oidcSub }.shouldNotContain(oidcSub)
    }

    override fun assertUserExists(user: User) {
        underTest.usersView[user.id].shouldNotBeNull {
            id shouldBe user.id
            oidcSub shouldBe user.oidcSub
            displayName shouldBe user.displayName
            createdAt shouldBe user.createdAt.truncatedTo(precision)
            updatedAt shouldBe user.updatedAt.truncatedTo(precision)
        }
    }

    override fun createUser(userDetails: UserDetails): User = underTest.create(userDetails).shouldBeSuccess()
}