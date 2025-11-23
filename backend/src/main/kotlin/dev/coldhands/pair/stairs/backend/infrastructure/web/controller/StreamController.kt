package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.StreamInfo
import dev.coldhands.pair.stairs.backend.domain.stream.*
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.PatchStreamDto
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/streams")
class StreamController(
    private val streamDao: StreamDao,
    private val statsService: StatsService,
) {

    @GetMapping
    fun getStreams(): List<Stream> = streamDao.findAll()

    @GetMapping("/info")
    fun getStreamInfos(): List<StreamInfo> =
        streamDao.findAll().map { it.toInfo() }

    @PostMapping
    fun saveStream(@RequestBody streamDetails: StreamDetails): ResponseEntity<Stream> =
        streamDao.create(streamDetails)
            .map {
                ResponseEntity.status(201)
                    .body(it)
            }.mapFailure { TODO("Currently not handling errors when creating stream") }
            .get()

    @PatchMapping("/{id}")
    fun updateStream(
        @RequestBody dto: PatchStreamDto,
        @PathVariable("id") id: StreamId,
    ): ResponseEntity<out Stream> {
        return streamDao.update(streamId = id, archived = dto.archived)
            .map { updatedStream -> ResponseEntity.ok(updatedStream) }
            .mapFailure {
                when(it){
                    is StreamUpdateError.StreamNotFound -> ResponseEntity.notFound().build<Nothing>()
                }
            }
            .get()
    }

    @GetMapping("/{id}/stats")
    fun getStats(
        @PathVariable("id") id: StreamId,
        @RequestParam("startDate", required = false) requestedStartDate: LocalDate?,
        @RequestParam("endDate", required = false) requestedEndDate: LocalDate?,
    ): ResponseEntity<out StreamStats> {
        if ((requestedStartDate != null && requestedEndDate == null) ||
            (requestedStartDate == null && requestedEndDate != null) ||
            (requestedStartDate != null && requestedStartDate.isAfter(requestedEndDate))
        ) {
            return ResponseEntity.badRequest().build<Nothing>()
        }

        return streamDao.findById(id)
            ?.let { _ ->
                if (requestedStartDate != null && requestedEndDate != null) {
                    statsService.getStreamStatsBetween(id.value, requestedStartDate, requestedEndDate)
                } else {
                    statsService.getStreamStats(id.value)
                }
            }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build<Nothing>()
    }
}
