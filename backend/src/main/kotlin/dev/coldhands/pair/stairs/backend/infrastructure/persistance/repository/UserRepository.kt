package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<UserEntity, Long> {
    fun findByOidcSub(oidSub: String): UserEntity?
}