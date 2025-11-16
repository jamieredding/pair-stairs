package dev.coldhands.pair.stairs.backend.domain.developer

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.forkhandles.result4k.Result

interface DeveloperDao {

    fun findById(developerId: DeveloperId): Developer?
    fun findAllById(developerIds: List<DeveloperId>): List<Developer>
    fun findAll(): List<Developer>

    fun create(developerDetails: DeveloperDetails): Result<Developer, DeveloperCreateError>
    fun update(developerId: DeveloperId, archived: Boolean): Result<Developer, DeveloperUpdateError>
}

sealed class DeveloperCreateError {
    data class NameTooLong(val name: String) : DeveloperCreateError()
}

sealed class DeveloperUpdateError {
    data class DeveloperNotFound(val developerId: DeveloperId) : DeveloperUpdateError()
}