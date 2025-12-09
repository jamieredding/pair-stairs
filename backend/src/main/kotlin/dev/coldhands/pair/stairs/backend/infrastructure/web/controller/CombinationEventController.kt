package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.GetCombinationEventDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.PairStreamInfo
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/combinations/event")
class CombinationEventController(
    private val combinationEventService: CombinationEventService,
    private val developerDao: DeveloperDao,
    private val streamDao: StreamDao,
    @Value("\${app.combinations.event.pageSize}")
    private val pageSize: Int,
) {

    @GetMapping
    fun getCombinationEvents(
        @RequestParam("page") page: Int?,
    ): ResponseEntity<List<GetCombinationEventDto>> {
        val requestedPage = page ?: 0
        val developers = developerDao.findAll()
        val streams = streamDao.findAll()
        val body: List<GetCombinationEventDto> = combinationEventService.getCombinationEvents(requestedPage, pageSize)
            .map { event ->
                GetCombinationEventDto(
                    id = event.id,
                    date = event.date,
                    // todo this should be shared with CombinationCalculationController
                    combination = event.combination.map { pairStream ->
                        PairStreamInfo(
                            developers = pairStream.developerIds.map { developerId ->
                                developers.firstOrNull { it.id == developerId }?.toInfo()
                                    ?: error("bad times")
                            }.sorted(),
                            stream = streams.firstOrNull { it.id == pairStream.streamId }?.toInfo()
                                ?: error("bad times")
                        )
                    }
                        .sorted()
                )
            }
        return ResponseEntity.ok(body)
    }

    @PostMapping
    fun saveEvent(
        @RequestBody request: SaveCombinationEventDto,
    ): ResponseEntity<Void> {
        combinationEventService.saveEvent(request.date(), request.combination())
        return ResponseEntity.status(201).build()
    }

    @DeleteMapping("/{id}")
    fun deleteEvent(
        @PathVariable("id") id: CombinationEventId,
    ): ResponseEntity<Void> =
        try {
            combinationEventService.deleteEvent(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
}
