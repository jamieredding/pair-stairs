package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamDao
import dev.coldhands.pair.stairs.backend.domain.TeamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import dev.forkhandles.result4k.Success
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
    fun saveTeam(@Valid @RequestBody createTeamDto: CreateTeamDto): ResponseEntity<*> {
        val slug = Slug(createTeamDto.slug)
        teamDao.findBySlug(slug)?.also {
            return badRequest()
                .body(ErrorDto(errorCode = "DUPLICATE_SLUG"))
        }

        // todo http4k-vertical-slice should properly handle errors from this
        val team = (teamDao.create(TeamDetails(name = createTeamDto.name, slug = slug)) as Success).value

        return status(201)
            .body(team.toDto())
    }

    @GetMapping("/{slug}")
    fun getTeam(@PathVariable slug: Slug): ResponseEntity<*> =
        teamDao.findBySlug(slug)?.let { ok(it.toDto()) }
            ?: notFound().build<String>()

    @GetMapping
    fun getTeams(): List<TeamDto> = teamDao.findAll().map { it.toDto() }
}