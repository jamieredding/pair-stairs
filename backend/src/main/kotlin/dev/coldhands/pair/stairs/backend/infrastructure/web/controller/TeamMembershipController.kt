package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.UserDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDto
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamMembershipRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamMembershipDto
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/teams/{slug}/memberships")
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
class TeamMembershipController(
    private val teamRepository: TeamRepository,
    private val userDao: UserDao,
    private val teamMembershipRepository: TeamMembershipRepository,
) {

    @PostMapping
    fun saveTeamMembership(
        @PathVariable slug: String,
        @RequestBody body: CreateTeamMembershipDto
    ): ResponseEntity<*> {
        val team = teamRepository.findBySlug(slug)!! // todo handle missing
        val user = userDao.findById(body.userId)!! // todo handle missing

        val teamMembershipEntity = teamMembershipRepository.saveAndFlush(
            TeamMembershipEntity(
                displayName = user.displayName,
                user = user.toEntity(),
                team = team
            )
        )

        return status(201)
            .body(teamMembershipEntity.toDto())
    }

}