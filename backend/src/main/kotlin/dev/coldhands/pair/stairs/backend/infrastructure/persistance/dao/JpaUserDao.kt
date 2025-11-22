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

    override fun update(userId: UserId, displayName: String): Result<User, UserUpdateError> {
        displayName.also {
            if (it.length > 255) return UserUpdateError.DisplayNameTooLong(it).asFailure()
        }

        val existingUser = userRepository.findById(userId.value).getOrNull()
            ?: return UserUpdateError.UserNotFound(userId).asFailure()

        val entityToUpdate = existingUser.apply {
            this.displayName = displayName
            this.updatedAt = dateProvider.instant().truncatedTo(precision)
        }

        return userRepository.save(entityToUpdate).toDomain().asSuccess()
    }
}