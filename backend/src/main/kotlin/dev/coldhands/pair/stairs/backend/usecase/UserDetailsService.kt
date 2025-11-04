package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.*
import dev.forkhandles.result4k.Result

class UserDetailsService(
    private val userDao: UserDao,
) {
    private val userDisplayNameService: UserDisplayNameService = UserDisplayNameService()

    fun createOrUpdate(
        oidcSub: OidcSub,
        userName: UserName,
    ): Result<User, Any> { // todo http4k-vertical-slice the any here isn't ideal
        val displayName = userDisplayNameService.getDisplayNameFor(userName)

        val persistedUser = userDao.findByOidcSub(oidcSub)
            ?.copy(displayName = displayName)
            ?.let { userDao.update(it) }
            ?: userDao.create(UserDetails(oidcSub = oidcSub, displayName = displayName))

        return persistedUser
    }

    fun getUserByOidcSub(oidcSub: OidcSub): User? =
        userDao.findByOidcSub(oidcSub)
}
