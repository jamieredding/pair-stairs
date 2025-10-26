package dev.coldhands.pair.stairs.backend

import org.http4k.core.*
import org.http4k.filter.ServerFilters.CatchLensFailure
import org.http4k.lens.Path
import org.http4k.lens.int
import org.http4k.routing.bind
import org.http4k.routing.routes

class FakeRecorderHttp: HttpHandler {
    val calls = mutableListOf<Int>()

    private val answer = Path.int().of("answer")

    private fun handleRequest(request: Request): Response {
        calls.add(answer(request))
        return Response(Status.OK)
    }

    private val app = CatchLensFailure.then(
        routes(
            "/{answer}" bind Method.POST to ::handleRequest
        )
    )

    override fun invoke(request: Request): Response = app(request)
}