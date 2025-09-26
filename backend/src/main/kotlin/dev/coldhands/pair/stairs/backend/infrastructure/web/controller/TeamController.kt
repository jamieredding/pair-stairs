package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDto
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/teams")
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
class TeamController(private val teamRepository: TeamRepository) {

    @PostMapping
    fun saveTeam(@Valid @RequestBody createTeamDto: CreateTeamDto): ResponseEntity<*> {
        teamRepository.findBySlug(createTeamDto.slug)?.also {
            return badRequest()
                .body(ErrorDto(errorCode = "DUPLICATE_SLUG"))
        }

        val teamEntity = teamRepository.save(TeamEntity(name = createTeamDto.name, slug = createTeamDto.slug))

        return status(201)
            .body(teamEntity.toDto())
    }

    @GetMapping("/{slug}")
    fun getTeam(@PathVariable slug: String): ResponseEntity<*> =
        teamRepository.findBySlug(slug)?.let { ok(it.toDto()) }
            ?: notFound().build<String>()

    @GetMapping
    fun getTeams(): List<TeamDto> = teamRepository.findAll().map { it.toDto() }
}