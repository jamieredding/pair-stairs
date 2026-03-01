package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.infrastructure.Settings
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.web.security.OAuthSecureHandler
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.ResourceLoader
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.security.AccessToken
import kotlin.time.Clock

class AppHttpHandler(
    developerDao: DeveloperDao,
    streamDao: StreamDao,
    teamDao: TeamDao,
    statsService: StatsService,
    combinationCalculationService: CombinationCalculationService,
    combinationEventService: CombinationEventService,
    combinationMapper: CombinationMapper,
    oAuthClient: HttpHandler,
    clock: Clock,
    settings: Settings,
    cookieTokenStore: MutableMap<String, AccessToken>,
) : HttpHandler {

    private val apiRoutes = routes(
        DeveloperHandler(developerDao, statsService),
        StreamHandler(streamDao, statsService),
        CombinationCalculationHandler(
            combinationCalculationService,
            combinationMapper,
            settings.combinationsCalculatePageSize
        ),
        CombinationEventHandler(combinationEventService, combinationMapper, settings.combinationsEventPageSize),
        TeamHandler(teamDao)
    )

    private val oAuthSecureHandler = OAuthSecureHandler(
        routes = OAuthSecureHandler.Routes(
            apiRoutes = apiRoutes,
            authFilteredRoutes = singlePageApp(ResourceLoader.Directory(settings.staticContentPath.toFile().absolutePath))
        ),
        oAuthSettings = settings.oauth,
        oAuthClient = oAuthClient,
        clock = clock,
        cookieTokenStore = cookieTokenStore
    )

    override fun invoke(request: Request): Response = oAuthSecureHandler(request)

}