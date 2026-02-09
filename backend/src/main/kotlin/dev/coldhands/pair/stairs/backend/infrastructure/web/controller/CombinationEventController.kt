package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.combination.PairStream
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.GetCombinationEventDto
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
    private val combinationMapper: CombinationMapper,
    @Value("\${app.combinations.event.pageSize}")
    private val pageSize: Int,
) {

    @GetMapping
    fun getCombinationEvents(
        @RequestParam("page") page: Int?,
    ): ResponseEntity<List<GetCombinationEventDto>> {
        val requestedPage = page ?: 0
        val body: List<GetCombinationEventDto> = combinationEventService.getCombinationEvents(requestedPage, pageSize)
            .map { event ->
                GetCombinationEventDto(
                    id = event.id,
                    date = event.date,
                    combination = combinationMapper.toInfo(event.combination)
                )
            }
        return ResponseEntity.ok(body)
    }

    @PostMapping
    fun saveEvent(
        @RequestBody request: SaveCombinationEventDto,
    ): ResponseEntity<Void> {
        combinationEventService.saveEvent(request.date(), request.combination().map {
            PairStream(
                it.developerIds().toSet(),
                it.streamId()
            )
        })
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
