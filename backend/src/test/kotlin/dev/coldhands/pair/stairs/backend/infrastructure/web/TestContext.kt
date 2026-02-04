package dev.coldhands.pair.stairs.backend.infrastructure.web

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeDeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.web.handler.DeveloperHandler
import org.http4k.core.HttpHandler

fun testContext(testBody: TestContext.() -> Unit) {
    testBody(TestContext())
}

class TestContext {

    val developerDao = FakeDeveloperDao()
    val underTest: HttpHandler = DeveloperHandler(developerDao)
}