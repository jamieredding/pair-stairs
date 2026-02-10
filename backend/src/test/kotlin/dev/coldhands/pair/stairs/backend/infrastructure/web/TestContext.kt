package dev.coldhands.pair.stairs.backend.infrastructure.web

import dev.coldhands.pair.stairs.backend.FakeDateProvider
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeCombinationEventDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeDeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeStreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeTeamDao
import dev.coldhands.pair.stairs.backend.infrastructure.web.handler.AppHttpHandler
import dev.coldhands.pair.stairs.backend.usecase.*
import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.core.HttpHandler
import org.http4k.lens.int
import java.time.temporal.ChronoUnit

fun testContext(testBody: TestContext.() -> Unit) {
    testBody(TestContext())
}

class TestContext {

    val combinationsCalculatePageSizeLens = EnvironmentKey.int().required("app.combinations.calculate.pageSize")
    val combinationsEventPageSizeLens = EnvironmentKey.int().required("app.combinations.event.pageSize")
    var environment: Environment = Environment.fromResource("application.properties")

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


    val underTest: HttpHandler = { request ->
        val combinationsCalculatePageSize = combinationsCalculatePageSizeLens(environment)
        val combinationsEventPageSize = combinationsEventPageSizeLens(environment)

        val appHttpHandler = AppHttpHandler(
            developerDao,
            streamDao,
            teamDao,
            statsService,
            combinationCalculationService,
            combinationEventService,
            combinationMapper,
            combinationsCalculatePageSize,
            combinationsEventPageSize,
        )
        appHttpHandler(request)
    }
}