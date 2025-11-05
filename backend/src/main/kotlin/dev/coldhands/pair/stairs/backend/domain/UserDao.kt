package dev.coldhands.pair.stairs.backend.domain

import dev.forkhandles.result4k.Result

interface UserDao {
    fun findById(userId: UserId): User?
    fun findByOidcSub(oidcSub: OidcSub): User?

    fun create(userDetails: UserDetails): Result<User, UserCreateError>
    fun update(user: User): Result<User, UserUpdateError>
}

sealed class UserCreateError {
    data class DuplicateOidcSub(val oidcSub: OidcSub) : UserCreateError()
    data class OidcSubTooLong(val oidcSub: OidcSub) : UserCreateError()
    data class DisplayNameTooLong(val displayName: String) : UserCreateError()
}

sealed class UserUpdateError {
    data class UserNotFound(val userId: UserId) : UserUpdateError()
    object CannotChangeOidcSub : UserUpdateError()
    object CannotChangeCreatedAt : UserUpdateError()
    object CannotChangeUpdatedAt : UserUpdateError()
    data class DisplayNameTooLong(val displayName: String) : UserUpdateError()
}