package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorCode
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

object TeamHandler {
    private val pathSlugLens = Path.map { Slug(it) }.of("slug")
    private val errorBodyLens = Body.auto<ErrorDto>().toLens()
    private val teamDtoLens = Body.auto<TeamDto>().toLens()
    private val teamsListLens = Body.auto<List<TeamDto>>().toLens()

    operator fun invoke(
        teamDao: TeamDao,
    ): RoutingHttpHandler = routes(
        "/api/v1/teams/{slug}" bind GET to {
            val slug = pathSlugLens(it)
            teamDao.findBySlug(slug)?.let { team ->
                teamDtoLens(team.toDto(), Response(OK))
            }
                ?: errorBodyLens(ErrorDto(ErrorCode.TEAM_NOT_FOUND), Response(NOT_FOUND))
        },

        "/api/v1/teams" bind GET to {
            teamsListLens(
                teamDao.findAll()
                    .map { team -> team.toDto() },
                Response(OK)
            )
        }
    )
}