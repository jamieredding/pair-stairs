package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperInfo
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeDeveloperDao
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.*
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.routes

object DeveloperHandler {

    private val pathDeveloperIdLens = Path.map { DeveloperId(it.toLong()) }.of("id")

    private val developerListLens = Body.auto<List<Developer>>().toLens()
    private val postDeveloperDetailsName = Body.auto<PostDeveloperDetails>().map { it.name }.toLens()
    private val patchDeveloperArchived = Body.auto<PatchDeveloperDetails>().map { it.archived }.toLens()
    private val developerLens = Body.auto<Developer>().toLens()
    private val developerInfoListLens = Body.auto<List<DeveloperInfo>>().toLens()

    private data class PostDeveloperDetails(
        val name: String,
    )

    private data class PatchDeveloperDetails(
        val archived: Boolean,
    )

    operator fun invoke(developerDao: FakeDeveloperDao): HttpHandler =
        routes(
            "/api/v1/developers" bind GET to {
                val developers = developerDao.findAll()

                Response(OK).with(developerListLens of developers)
            },

            "/api/v1/developers" bind POST to { request ->
                val requestedName = postDeveloperDetailsName(request)

                developerDao.create(
                    DeveloperDetails(
                        name = requestedName,
                        archived = false
                    )
                ).map {
                    Response(CREATED)
                        .with(developerLens of it)
                }
                    .mapFailure { TODO("TEAMS-FIRST-PASS Currently not handling errors when creating developer") }
                    .get()
            },

            "/api/v1/developers/{id}" bind PATCH to { request ->
                val developerId = pathDeveloperIdLens(request)
                val newArchived = patchDeveloperArchived(request)

//                val developer = developerDao.findById(developerId)!!

                val updatedDeveloper = developerDao.update(developerId, newArchived)
                    .mapFailure { TODO("Currently unsupported?") }
                    .get()


                Response(OK)
                    .with(developerLens of updatedDeveloper)
            },

            "/api/v1/developers/info" bind GET to {
                val developers = developerDao.findAll()
                    .map { it.toInfo() }

                Response(OK).with(developerInfoListLens of developers)
            }
        )
}