package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DeveloperRepository: JpaRepository<DeveloperEntity, Long>