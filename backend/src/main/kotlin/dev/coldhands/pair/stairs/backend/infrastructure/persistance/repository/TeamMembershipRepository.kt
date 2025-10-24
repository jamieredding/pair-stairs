package dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TeamMembershipRepository: JpaRepository<TeamMembershipEntity, Long>