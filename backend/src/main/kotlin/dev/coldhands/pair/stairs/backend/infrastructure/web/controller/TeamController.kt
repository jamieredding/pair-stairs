package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDto
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}