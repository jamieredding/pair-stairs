package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.coldhands.pair.stairs.backend.domain.user.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.time.temporal.TemporalUnit
import java.util.Collections.unmodifiableMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FakeUserDao(
    private val dateProvider: DateProvider,
    private val precision: TemporalUnit
) : UserDao {
    private val users = ConcurrentHashMap<UserId, User>()
    private var nextId = AtomicLong(0L);
    val usersView: Map<UserId, User> = unmodifiableMap(users)

    override fun findById(userId: UserId): User? = users[userId]

    override fun findByOidcSub(oidcSub: OidcSub): User? = users.values.find { it.oidcSub == oidcSub }

    override fun create(userDetails: UserDetails): Result<User, UserCreateError> {
        userDetails.oidcSub.also {
            if (it.value.length > 255) return UserCreateError.OidcSubTooLong(it).asFailure()
        }
        userDetails.displayName.also {
            if (it.length > 255) return UserCreateError.DisplayNameTooLong(it).asFailure()
        }
        if (users.any { (_, user) -> user.oidcSub == userDetails.oidcSub }) {
            return UserCreateError.DuplicateOidcSub(userDetails.oidcSub).asFailure()
        }

        val userId = UserId(nextId.getAndIncrement())
        val user = User(
            id = userId,
            oidcSub = userDetails.oidcSub,
            displayName = userDetails.displayName,
            createdAt = dateProvider.instant().truncatedTo(precision),
            updatedAt = dateProvider.instant().truncatedTo(precision)
        )
        users[userId] = user
        return user.asSuccess()
    }

    override fun update(user: User): Result<User, UserUpdateError> {
        users[user.id]?.let { existingUser ->
            if (existingUser.oidcSub != user.oidcSub) return UserUpdateError.CannotChangeOidcSub.asFailure()
            if (existingUser.createdAt != user.createdAt.truncatedTo(precision)) return UserUpdateError.CannotChangeCreatedAt.asFailure()
            if (existingUser.updatedAt != user.updatedAt.truncatedTo(precision)) return UserUpdateError.CannotChangeUpdatedAt.asFailure()

            user.displayName.also {
                if (it.length > 255) return UserUpdateError.DisplayNameTooLong(it).asFailure()
            }
        } ?: UserUpdateError.UserNotFound(user.id).asFailure()

        return users.computeIfPresent(user.id) { _, existingUser ->
            val updatedUser = existingUser.copy(
                displayName = user.displayName,
                updatedAt = dateProvider.instant().truncatedTo(precision)
            )
            updatedUser
        }?.asSuccess() ?: UserUpdateError.UserNotFound(user.id).asFailure()
    }
}