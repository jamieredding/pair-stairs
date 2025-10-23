package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.User
import dev.coldhands.pair.stairs.backend.domain.UserName
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.UserMapper
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository

class UserDetailsService(
    private val userRepository: UserRepository,
) {
    private val userDisplayNameService: UserDisplayNameService = UserDisplayNameService()

    fun createOrUpdate(
        oidcSub: String,
        userName: UserName,
    ): User {
        val displayName = userDisplayNameService.getDisplayNameFor(userName)

        val toPersist = userRepository.findByOidcSub(oidcSub)
            ?.apply { this.displayName = displayName }
            ?: UserEntity(oidcSub = oidcSub, displayName = displayName)

        return UserMapper.entityToDomain(userRepository.saveAndFlush(toPersist))
    }

    fun getUserByOidcSub(oidcSub: String): User? =
        userRepository.findByOidcSub(oidcSub)
            ?.let(UserMapper::entityToDomain)
}
