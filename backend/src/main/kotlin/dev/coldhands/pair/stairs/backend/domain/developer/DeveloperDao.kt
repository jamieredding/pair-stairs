package dev.coldhands.pair.stairs.backend.domain.developer

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.forkhandles.result4k.Result

interface DeveloperDao {

    fun findById(developerId: DeveloperId): Developer?
    fun findAllById(developerIds: List<DeveloperId>): List<Developer>
    fun findAll(): List<Developer>

    fun create(developerDetails: DeveloperDetails): Result<Developer, Any>
}