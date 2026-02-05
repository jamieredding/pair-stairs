package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.stream.*
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorCode
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorCode.STREAM_NOT_FOUND
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.core.Body
import org.http4k.core.Method.*
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.localDate
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

object StreamHandler {

    private val pathStreamIdLens = Path.map { StreamId(it.toLong()) }.of("id")
    private val queryStartDateLens = Query.localDate().optional("startDate")
    private val queryEndDateLens = Query.localDate().optional("endDate")

    private val streamListLens = Body.auto<List<Stream>>().toLens()
    private val postStreamDetailsName = Body.auto<PostStreamDetails>().map { it.name }.toLens()
    private val patchStreamArchived = Body.auto<PatchStreamDetails>().map { it.archived }.toLens()
    private val streamLens = Body.auto<Stream>().toLens()
    private val streamInfoListLens = Body.auto<List<StreamInfo>>().toLens()
    private val streamStatsLens = Body.auto<StreamStats>().toLens()
    private val errorBodyLens = Body.auto<ErrorDto>().toLens()

    private data class PostStreamDetails(
        val name: String,
    )

    private data class PatchStreamDetails(
        val archived: Boolean,
    )

    operator fun invoke(streamDao: StreamDao, statsService: StatsService): RoutingHttpHandler =
        routes(
            "/api/v1/streams" bind GET to {
                val streams = streamDao.findAll()

                Response(OK).with(streamListLens of streams)
            },

            "/api/v1/streams" bind POST to { request ->
                val requestedName = postStreamDetailsName(request)

                streamDao.create(
                    StreamDetails(
                        name = requestedName,
                        archived = false
                    )
                ).map {
                    Response(CREATED)
                        .with(streamLens of it)
                }
                    .mapFailure { TODO("TEAMS-FIRST-PASS Currently not handling errors when creating stream") }
                    .get()
            },

            "/api/v1/streams/{id}" bind PATCH to { request ->
                val streamId = pathStreamIdLens(request)
                val newArchived = patchStreamArchived(request)

                streamDao.update(streamId, newArchived)
                    .map { updatedStream ->
                        Response(OK)
                            .with(streamLens of updatedStream)
                    }
                    .mapFailure {
                        when (it) {
                            is StreamUpdateError.StreamNotFound -> Response(NOT_FOUND)
                                .with(errorBodyLens of ErrorDto(errorCode = STREAM_NOT_FOUND))
                        }
                    }
                    .get()
            },

            "/api/v1/streams/info" bind GET to {
                val streams = streamDao.findAll()
                    .map { it.toInfo() }

                Response(OK).with(streamInfoListLens of streams)
            },

            "/api/v1/streams/{id}/stats" bind GET to { request ->
                val startDate = queryStartDateLens(request)
                val endDate = queryEndDateLens(request)
                when {
                    (startDate == null) != (endDate == null) -> Response(BAD_REQUEST).with(
                        errorBodyLens of ErrorDto(
                            errorCode = ErrorCode.BAD_REQUEST,
                            errorMessage = "startDate and endDate must be provided"
                        )
                    )

                    startDate != null && startDate > endDate -> Response(BAD_REQUEST).with(
                        errorBodyLens of ErrorDto(
                            errorCode = ErrorCode.BAD_REQUEST,
                            errorMessage = "startDate must be before endDate"
                        )
                    )

                    else -> {
                        val streamId = pathStreamIdLens(request)

                        streamDao.findById(streamId)?.let {
                            when {
                                startDate != null && endDate != null -> statsService.getStreamStatsBetween(
                                    streamId,
                                    startDate,
                                    endDate
                                )

                                else -> statsService.getStreamStats(streamId)
                            }.toResponse()
                        }
                            ?: Response(NOT_FOUND)
                                .with(errorBodyLens of ErrorDto(errorCode = STREAM_NOT_FOUND))

                    }
                }
            }
        )

    private fun StreamStats.toResponse(): Response {
        return Response(OK)
            .with(streamStatsLens of this)
    }
}
