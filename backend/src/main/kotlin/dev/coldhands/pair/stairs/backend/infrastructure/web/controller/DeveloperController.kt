package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperUpdateError
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.PatchDeveloperDto
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/developers")
class DeveloperController(
    private val developerDao: DeveloperDao,
    private val statsService: StatsService
) {

    @GetMapping
    fun getDevelopers(): List<Developer> = developerDao.findAll()

    @GetMapping("/info")
    fun getDeveloperInfos() = developerDao.findAll().map { it.toInfo() }

    @PostMapping
    fun saveDeveloper(@RequestBody developerDetails: DeveloperDetails): ResponseEntity<*> =
        developerDao.create(developerDetails)
            .map {
                ResponseEntity.status(201)
                    .body(it)
            }
            .mapFailure { TODO("Currently not handling errors when creating developer") }
            .get()

    @PatchMapping("/{id}")
    fun updateDeveloper(
        @RequestBody dto: PatchDeveloperDto,
        @PathVariable("id") id: DeveloperId
    ): ResponseEntity<*> =
        developerDao.update(developerId = id, archived = dto.archived)
            .map { updatedDeveloper -> ResponseEntity.ok(updatedDeveloper) }
            .mapFailure {
                when (it) {
                    is DeveloperUpdateError.DeveloperNotFound -> ResponseEntity.notFound().build<Nothing>()
                }
            }
            .get()

    @GetMapping("/{id}/stats")
    fun getStats(
        @PathVariable("id") id: DeveloperId,
        @RequestParam("startDate") requestedStartDate: LocalDate?,
        @RequestParam("endDate") requestedEndDate: LocalDate?
    ): ResponseEntity<*> {
        if ((requestedStartDate != null && requestedEndDate == null) ||
            (requestedStartDate == null && requestedEndDate != null) ||
            (requestedStartDate != null && requestedStartDate.isAfter(requestedEndDate))
        ) {
            return ResponseEntity.badRequest().build<Nothing>()
        }

        return developerDao.findById(id)
            ?.let { _ ->
                if (requestedStartDate != null && requestedEndDate != null) {
                    statsService.getDeveloperStatsBetween(id.value, requestedStartDate, requestedEndDate)
                } else {
                    statsService.getDeveloperStats(id.value)
                }
            }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build<Nothing>()
    }
}