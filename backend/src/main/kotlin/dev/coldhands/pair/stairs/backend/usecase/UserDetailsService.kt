package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.user.User
import dev.coldhands.pair.stairs.backend.domain.user.UserDao
import dev.coldhands.pair.stairs.backend.domain.user.UserDetails
import dev.coldhands.pair.stairs.backend.domain.user.UserName
import dev.forkhandles.result4k.Result

class UserDetailsService(
    private val userDao: UserDao,
) {
    private val userDisplayNameService: UserDisplayNameService = UserDisplayNameService()

    fun createOrUpdate(
        oidcSub: OidcSub,
        userName: UserName,
    ): Result<User, Any> {
        val displayName = userDisplayNameService.getDisplayNameFor(userName)

        val persistedUser = userDao.findByOidcSub(oidcSub)
            ?.let { userDao.update(it.id, displayName) }
            ?: userDao.create(UserDetails(oidcSub = oidcSub, displayName = displayName))

        return persistedUser
    }

    fun getUserByOidcSub(oidcSub: OidcSub): User? =
        userDao.findByOidcSub(oidcSub)
}
