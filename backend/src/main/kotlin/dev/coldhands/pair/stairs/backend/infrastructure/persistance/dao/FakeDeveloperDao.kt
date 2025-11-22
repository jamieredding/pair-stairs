package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.developer.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.util.Collections.unmodifiableMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FakeDeveloperDao : DeveloperDao {
    private val developers = ConcurrentHashMap<DeveloperId, Developer>()
    val developersView: Map<DeveloperId, Developer> = unmodifiableMap(developers)
    private var nextId = AtomicLong(0L)

    override fun findById(developerId: DeveloperId): Developer? = developers[developerId]

    override fun findAllById(developerIds: List<DeveloperId>): List<Developer> =
        developers.values.filter { it.id in developerIds }

    override fun findAll(): List<Developer> = developers.values.toList()

    override fun create(developerDetails: DeveloperDetails): Result<Developer, DeveloperCreateError> {
        developerDetails.name.also {
            if (it.length > 255) return DeveloperCreateError.NameTooLong(it).asFailure()
        }

        val developerId = DeveloperId(nextId.getAndIncrement())
        val developer = Developer(
            id = developerId,
            name = developerDetails.name,
            archived = developerDetails.archived,
        )
        developers[developerId] = developer
        return developer.asSuccess()
    }

    override fun update(
        developerId: DeveloperId,
        archived: Boolean
    ): Result<Developer, DeveloperUpdateError> {
        return developers.computeIfPresent(developerId) { _, existingDeveloper ->
            val updatedDeveloper = existingDeveloper.copy(archived = archived)
            updatedDeveloper
        }?.asSuccess() ?: DeveloperUpdateError.DeveloperNotFound(developerId).asFailure()
    }
}