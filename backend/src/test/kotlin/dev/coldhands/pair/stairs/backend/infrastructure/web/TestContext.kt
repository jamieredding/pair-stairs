package dev.coldhands.pair.stairs.backend.infrastructure.web

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.infrastructure.Settings
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeCombinationEventDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeDeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeStreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeTeamDao
import dev.coldhands.pair.stairs.backend.infrastructure.web.handler.AppHttpHandler
import dev.coldhands.pair.stairs.backend.usecase.*
import org.http4k.client.JavaHttpClient
import org.http4k.config.Environment
import org.http4k.core.HttpHandler
import org.http4k.security.AccessToken
import java.time.temporal.ChronoUnit
import kotlin.time.Clock

fun testContext(testBody: TestContext.() -> Unit) {
    testBody(TestContext())
}

class TestContext {
    var environment: Environment = Environment.from("static.content.path" to "src/test/resources/static-test")
        .overrides(Environment.fromResource("application.properties"))

    val developerDao = FakeDeveloperDao()
    val streamDao = FakeStreamDao()
    val combinationEventDao = FakeCombinationEventDao(developerDao, streamDao)
    val teamDao = FakeTeamDao(FakeDateProvider(), ChronoUnit.MILLIS)

    val statsService = StatsService(developerDao, streamDao, combinationEventDao)
    val combinationEventService = CombinationEventService(combinationEventDao)
    val combinationHistoryRepository = BackendCombinationHistoryRepository(combinationEventDao)
    val combinationCalculationService =
        CoreCombinationCalculationService(EntryPointFactory(combinationHistoryRepository))
    val combinationMapper = CombinationMapper(developerDao, streamDao)
    val cookieTokenStore = mutableMapOf<String, AccessToken>() // todo don't expose http4k access token here

    val underTest: HttpHandler = { request ->
        val settings = Settings(environment)

        val appHttpHandler = AppHttpHandler(
            developerDao,
            streamDao,
            teamDao,
            statsService,
            combinationCalculationService,
            combinationEventService,
            combinationMapper,
            JavaHttpClient(), // todo inject this
            Clock.System,
            settings,
            cookieTokenStore,
        )
        appHttpHandler(request)
    }
}