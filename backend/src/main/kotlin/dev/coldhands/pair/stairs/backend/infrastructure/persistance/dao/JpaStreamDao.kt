package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.stream.*
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import kotlin.jvm.optionals.getOrNull

class JpaStreamDao(private val streamRepository: StreamRepository) : StreamDao {
    override fun findById(streamId: StreamId): Stream? = streamRepository.findById(streamId.value)
        .getOrNull()
        ?.toDomain()

    override fun findAllById(streamIds: List<StreamId>): List<Stream> =
        streamRepository.findAllById(streamIds.map { it.value })
            .map { it.toDomain() }

    override fun findAll(): List<Stream> = streamRepository.findAll().map { it.toDomain() }

    override fun create(streamDetails: StreamDetails): Result<Stream, StreamCreateError> {
        streamDetails.name.also {
            if (it.length > 255) return StreamCreateError.NameTooLong(it).asFailure()
        }

        return streamRepository.save(
            StreamEntity(
                name = streamDetails.name,
                archived = streamDetails.archived,
            )
        ).toDomain().asSuccess()
    }

    override fun update(streamId: StreamId, archived: Boolean): Result<Stream, StreamUpdateError> =
        streamRepository.findById(streamId.value).getOrNull()
            ?.apply {
                this.archived = archived
            }
            ?.let { entity -> streamRepository.save(entity).toDomain().asSuccess() }
            ?: StreamUpdateError.StreamNotFound(streamId).asFailure()
}