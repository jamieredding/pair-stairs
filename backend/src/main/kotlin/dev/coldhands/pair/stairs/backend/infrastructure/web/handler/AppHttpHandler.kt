package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.then
import org.http4k.routing.routes

class AppHttpHandler(
    developerDao: DeveloperDao,
    streamDao: StreamDao,
    teamDao: TeamDao,
    statsService: StatsService,
    combinationCalculationService: CombinationCalculationService,
    combinationEventService: CombinationEventService,
    combinationMapper: CombinationMapper,
    combinationsCalculatePageSize: Int,
    combinationsEventPageSize: Int,
): HttpHandler {

    private val mainAppRoutes = routes(
        DeveloperHandler(developerDao, statsService),
        StreamHandler(streamDao, statsService),
        CombinationCalculationHandler(combinationCalculationService, combinationMapper, combinationsCalculatePageSize),
        CombinationEventHandler(combinationEventService, combinationMapper, combinationsEventPageSize),
        TeamHandler(teamDao)
    )

    private val appStack: HttpHandler = CatchLensFailureFilter()
        .then(mainAppRoutes)

    override fun invoke(request: Request): Response = appStack(request)

}