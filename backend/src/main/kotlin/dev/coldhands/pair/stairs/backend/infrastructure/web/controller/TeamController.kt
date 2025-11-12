package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.team.TeamCreateError
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/teams")
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
class TeamController(private val teamDao: TeamDao) {

    @PostMapping
    fun saveTeam(@Valid @RequestBody createTeamDto: CreateTeamDto): ResponseEntity<*> =
        teamDao.create(TeamDetails(name = createTeamDto.name, slug = Slug(createTeamDto.slug)))
            .map {
                status(201)
                    .body(it.toDto())
            }
            .mapFailure {
                when (it) {
                    is TeamCreateError.DuplicateSlug -> "DUPLICATE_SLUG"
                    is TeamCreateError.NameTooLong -> "NAME_TOO_LONG"
                    is TeamCreateError.SlugTooLong -> "SLUG_TOO_LONG"
                }
            }
            .mapFailure {
                badRequest()
                    .body(ErrorDto(errorCode = it))
            }
            .get()

    @GetMapping("/{slug}")
    fun getTeam(@PathVariable slug: Slug): ResponseEntity<*> =
        teamDao.findBySlug(slug)?.let { ok(it.toDto()) }
            ?: notFound().build<String>()

    @GetMapping
    fun getTeams(): List<TeamDto> = teamDao.findAll().map { it.toDto() }
}