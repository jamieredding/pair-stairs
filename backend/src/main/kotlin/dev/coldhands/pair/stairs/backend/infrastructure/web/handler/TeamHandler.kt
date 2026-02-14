package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.team.TeamCreateError
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorCode
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import dev.forkhandles.result4k.*
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
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
    private val createTeamDtoLens = Body.auto<CreateTeamDto>().toLens()

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
        },

        "api/v1/teams" bind POST to route@{
            val dto = createTeamDtoLens(it)

            with(dto) {
                when {
                    name.isBlank() -> ErrorCode.INVALID_NAME.asFailure()
                    slug.isBlank() -> ErrorCode.INVALID_SLUG.asFailure()
                    slug.matches(Regex("^[a-z0-9-]+$")).not() -> ErrorCode.INVALID_SLUG.asFailure()
                    else -> asSuccess()
                }
                    .failureOrNull()
                    ?.let { errorCode ->
                        return@route errorBodyLens(ErrorDto(errorCode), Response(BAD_REQUEST))
                    }
            }


            teamDao.create(
                TeamDetails(
                    name = dto.name,
                    slug = Slug(dto.slug),
                )
            ).map { createdTeam -> teamDtoLens(createdTeam.toDto(), Response(CREATED)) }
                .mapFailure { teamCreateError ->
                    val errorCode = when (teamCreateError) {
                        is TeamCreateError.DuplicateSlug -> ErrorCode.DUPLICATE_SLUG
                        is TeamCreateError.NameTooLong -> ErrorCode.NAME_TOO_LONG
                        is TeamCreateError.SlugTooLong -> ErrorCode.SLUG_TOO_LONG
                    }
                    errorBodyLens(ErrorDto(errorCode), Response(BAD_REQUEST))
                }
                .get()

        }
    )
}