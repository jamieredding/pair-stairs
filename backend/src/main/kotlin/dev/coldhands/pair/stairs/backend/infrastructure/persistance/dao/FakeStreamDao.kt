package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.stream.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.util.Collections.unmodifiableMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FakeStreamDao : StreamDao {
    private val streams = ConcurrentHashMap<StreamId, Stream>()
    val streamsView: Map<StreamId, Stream> = unmodifiableMap(streams)
    private var nextId = AtomicLong(0L)

    override fun findById(streamId: StreamId): Stream? = streams[streamId]

    override fun findAllById(streamIds: List<StreamId>): List<Stream> =
        streams.values.filter { it.id in streamIds }

    override fun findAll(): List<Stream> = streams.values.toList()

    override fun create(streamDetails: StreamDetails): Result<Stream, StreamCreateError> {
        streamDetails.name.also {
            if (it.length > 255) return StreamCreateError.NameTooLong(it).asFailure()
        }

        val streamId = StreamId(nextId.getAndIncrement())
        val stream = Stream(
            id = streamId,
            name = streamDetails.name,
            archived = streamDetails.archived,
        )
        streams[streamId] = stream
        return stream.asSuccess()
    }

    override fun update(
        streamId: StreamId,
        archived: Boolean
    ): Result<Stream, StreamUpdateError> {
        return streams.computeIfPresent(streamId) { _, existingStream ->
            val updatedStream = existingStream.copy(archived = archived)
            updatedStream
        }?.asSuccess() ?: StreamUpdateError.StreamNotFound(streamId).asFailure()
    }
}