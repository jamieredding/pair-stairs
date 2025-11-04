package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository
import dev.forkhandles.result4k.*
import kotlin.jvm.optionals.getOrNull

class JpaUserDao(private val userRepository: UserRepository) : UserDao {

    override fun findById(userId: UserId): User? =
        userRepository.findById(userId.value)
            .getOrNull()
            ?.toDomain()

    override fun findByOidcSub(oidcSub: OidcSub): User? =
        userRepository.findByOidcSub(oidcSub.value)
            ?.toDomain()

    override fun create(userDetails: UserDetails): Result<User, UserCreateError> =
        resultFromCatching<Exception, User> {
            userDetails.oidcSub?.also {
                if (it.value.length > 255) return UserCreateError.OidcSubTooLong(it).asFailure()
            }
            userDetails.displayName?.also {
                if (it.length > 255) return UserCreateError.DisplayNameTooLong(it).asFailure()
            }

            userRepository.save( // todo http4k-vertical-slice should this be save and flush?
                UserEntity(
                    oidcSub = userDetails.oidcSub!!.value, // todo http4k-vertical-slice resolve nullability
                    displayName = userDetails.displayName!! // todo http4k-vertical-slice resolve nullability
                )
            ).toDomain()
        }.mapFailure { _ -> UserCreateError.DuplicateOidcSub(userDetails.oidcSub!!) }

    override fun update(user: User): Result<User, UserUpdateError> {
        val existingUser = userRepository.findById(user.id.value).getOrNull()
            ?: return UserUpdateError.UserNotFound(user.id).asFailure()

        if (existingUser.oidcSub != user.oidcSub.value) return UserUpdateError.CannotChangeOidcSub.asFailure()

        user.displayName.also {
            if (it.length > 255) return UserUpdateError.DisplayNameTooLong(it).asFailure()
        }

        val entityToUpdate = existingUser.apply {
            this.displayName = user.displayName
        }
        // todo http4k-vertical-slice saveAndFlush is required for UserDetailsServiceTest to pass, but not JpaUserDao
        //  this means either I should be exposing createdAt and updatedAt in domain User
        //  or remove them entirely
        return userRepository.saveAndFlush(entityToUpdate).toDomain().asSuccess()
    }
}