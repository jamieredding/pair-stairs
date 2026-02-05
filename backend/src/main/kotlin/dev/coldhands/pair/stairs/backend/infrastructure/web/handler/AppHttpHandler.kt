package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import org.http4k.core.HttpHandler
import org.http4k.routing.routes

object AppHttpHandler {

    operator fun invoke(developerDao: DeveloperDao, streamDao: StreamDao, statsService: StatsService): HttpHandler = routes(
        DeveloperHandler(developerDao, statsService),
        StreamHandler(streamDao, statsService)
    )
}