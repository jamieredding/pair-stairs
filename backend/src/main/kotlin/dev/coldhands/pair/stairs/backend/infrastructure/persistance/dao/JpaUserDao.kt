package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.coldhands.pair.stairs.backend.domain.user.*
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.time.temporal.TemporalUnit
import kotlin.jvm.optionals.getOrNull

class JpaUserDao(
    private val userRepository: UserRepository,
    private val dateProvider: DateProvider,
    private val precision: TemporalUnit
) : UserDao {

    override fun findById(userId: UserId): User? =
        userRepository.findById(userId.value)
            .getOrNull()
            ?.toDomain()

    override fun findByOidcSub(oidcSub: OidcSub): User? =
        userRepository.findByOidcSub(oidcSub.value)
            ?.toDomain()

    override fun create(userDetails: UserDetails): Result<User, UserCreateError> {
        findByOidcSub(userDetails.oidcSub)?.also {
            return UserCreateError.DuplicateOidcSub(userDetails.oidcSub).asFailure()
        }
        userDetails.oidcSub.also {
            if (it.value.length > 255) return UserCreateError.OidcSubTooLong(it).asFailure()
        }
        userDetails.displayName.also {
            if (it.length > 255) return UserCreateError.DisplayNameTooLong(it).asFailure()
        }

        return userRepository.save(
            UserEntity(
                oidcSub = userDetails.oidcSub.value,
                displayName = userDetails.displayName,
                createdAt = dateProvider.instant().truncatedTo(precision),
                updatedAt = dateProvider.instant().truncatedTo(precision)
            )
        ).toDomain().asSuccess()
    }

    // todo stop accepting user here and instead id and only allowed fields to update
    override fun update(user: User): Result<User, UserUpdateError> {
        val existingUser = userRepository.findById(user.id.value).getOrNull()
            ?: return UserUpdateError.UserNotFound(user.id).asFailure()

        if (existingUser.oidcSub != user.oidcSub.value) return UserUpdateError.CannotChangeOidcSub.asFailure()
        if (existingUser.createdAt != user.createdAt.truncatedTo(precision)) return UserUpdateError.CannotChangeCreatedAt.asFailure()
        if (existingUser.updatedAt != user.updatedAt.truncatedTo(precision)) return UserUpdateError.CannotChangeUpdatedAt.asFailure()

        user.displayName.also {
            if (it.length > 255) return UserUpdateError.DisplayNameTooLong(it).asFailure()
        }

        val entityToUpdate = existingUser.apply {
            this.displayName = user.displayName
            this.updatedAt = dateProvider.instant().truncatedTo(precision)
        }

        return userRepository.save(entityToUpdate).toDomain().asSuccess()
    }
}