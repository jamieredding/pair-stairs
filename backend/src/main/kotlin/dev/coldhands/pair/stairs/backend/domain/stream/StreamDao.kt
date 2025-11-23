package dev.coldhands.pair.stairs.backend.domain.stream

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.forkhandles.result4k.Result

interface StreamDao {

    fun findById(streamId: StreamId): Stream?
    fun findAllById(streamIds: List<StreamId>): List<Stream>
    fun findAll(): List<Stream>

    fun create(streamDetails: StreamDetails): Result<Stream, StreamCreateError>
    fun update(streamId: StreamId, archived: Boolean): Result<Stream, StreamUpdateError>
}

sealed class StreamCreateError {
    data class NameTooLong(val name: String) : StreamCreateError()
}

sealed class StreamUpdateError {
    data class StreamNotFound(val streamId: StreamId) : StreamUpdateError()
}