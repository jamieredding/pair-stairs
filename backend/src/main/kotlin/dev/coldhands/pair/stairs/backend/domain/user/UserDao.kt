package dev.coldhands.pair.stairs.backend.domain.user

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.forkhandles.result4k.Result

interface UserDao {
    fun findById(userId: UserId): User?
    fun findByOidcSub(oidcSub: OidcSub): User?

    fun create(userDetails: UserDetails): Result<User, UserCreateError>
    fun update(userId: UserId, displayName: String): Result<User, UserUpdateError>
}

sealed class UserCreateError {
    data class DuplicateOidcSub(val oidcSub: OidcSub) : UserCreateError()
    data class OidcSubTooLong(val oidcSub: OidcSub) : UserCreateError()
    data class DisplayNameTooLong(val displayName: String) : UserCreateError()
}

sealed class UserUpdateError {
    data class UserNotFound(val userId: UserId) : UserUpdateError()
    data class DisplayNameTooLong(val displayName: String) : UserUpdateError()
}