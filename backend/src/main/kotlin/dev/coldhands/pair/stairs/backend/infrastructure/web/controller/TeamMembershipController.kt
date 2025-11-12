package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamDao
import dev.coldhands.pair.stairs.backend.domain.UserDao
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipCreateError
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDao
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamMembershipDto
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/teams/{slug}/memberships")
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
class TeamMembershipController(
    private val teamDao: TeamDao,
    private val userDao: UserDao,
    private val teamMembershipDao: TeamMembershipDao,
) {

    @PostMapping
    fun saveTeamMembership(
        @PathVariable slug: Slug,
        @RequestBody body: CreateTeamMembershipDto
    ): ResponseEntity<*> {
        val team = teamDao.findBySlug(slug)!! // todo handle missing
        val user = userDao.findById(body.userId)!! // todo handle missing

        return teamMembershipDao.create(
            TeamMembershipDetails(
                displayName = user.displayName,
                userId = body.userId,
                teamId = team.id
            )
        )
            .map {
                status(201)
                    .body(it.toDto())
            }
            .mapFailure {
                when (it) {
                    is TeamMembershipCreateError.DisplayNameTooLongError -> TODO()
                    is TeamMembershipCreateError.TeamNotFoundError -> TODO()
                    is TeamMembershipCreateError.UserNotFoundError -> TODO()
                }
            }
            .mapFailure {
                status(400)
                    .body(TODO())
            }
            .get()
    }

}