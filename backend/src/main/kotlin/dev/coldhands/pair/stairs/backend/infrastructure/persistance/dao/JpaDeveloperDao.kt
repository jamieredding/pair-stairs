package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.developer.*
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import kotlin.jvm.optionals.getOrNull

class JpaDeveloperDao(private val developerRepository: DeveloperRepository) : DeveloperDao {
    override fun findById(developerId: DeveloperId): Developer? = developerRepository.findById(developerId.value)
        .getOrNull()
        ?.toDomain()

    override fun findAllById(developerIds: List<DeveloperId>): List<Developer> =
        developerRepository.findAllById(developerIds.map { it.value })
            .map { it.toDomain() }

    override fun findAll(): List<Developer> = developerRepository.findAll().map { it.toDomain() }

    override fun create(developerDetails: DeveloperDetails): Result<Developer, DeveloperCreateError> {
        developerDetails.name.also {
            if (it.length > 255) return DeveloperCreateError.NameTooLong(it).asFailure()
        }

        return developerRepository.save(
            DeveloperEntity(
                name = developerDetails.name,
                archived = developerDetails.archived,
            )
        ).toDomain().asSuccess()
    }

    override fun update(developerId: DeveloperId, archived: Boolean): Result<Developer, DeveloperUpdateError> =
        developerRepository.findById(developerId.value).getOrNull()
            ?.apply {
                this.archived = archived
            }
            ?.let { entity -> developerRepository.save(entity).toDomain().asSuccess() }
            ?: DeveloperUpdateError.DeveloperNotFound(developerId).asFailure()
}