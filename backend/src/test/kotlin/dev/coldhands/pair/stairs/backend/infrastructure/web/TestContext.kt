package dev.coldhands.pair.stairs.backend.infrastructure.web

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeCombinationEventDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeDeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeStreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.web.handler.DeveloperHandler
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import org.http4k.core.HttpHandler

fun testContext(testBody: TestContext.() -> Unit) {
    testBody(TestContext())
}

class TestContext {

    val developerDao = FakeDeveloperDao()
    val streamDao = FakeStreamDao()
    val combinationEventDao = FakeCombinationEventDao(developerDao, streamDao)

    val statsService = StatsService(developerDao, streamDao, combinationEventDao)
    val combinationEventService = CombinationEventService(combinationEventDao)

    val underTest: HttpHandler = DeveloperHandler(developerDao, statsService)
}