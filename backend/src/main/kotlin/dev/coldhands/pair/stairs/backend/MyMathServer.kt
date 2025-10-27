package dev.coldhands.pair.stairs.backend

import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ClientFilters.SetHostFrom
import org.http4k.filter.ServerFilters.CatchLensFailure
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    MyMathServer(8080, Uri.of("http://localhost:18082")).start()
}

fun MyMathServer(port: Int, recorderUri: Uri): Http4kServer =
    MyMathApp(recorderHttp = SetHostFrom(recorderUri).then(OkHttp()))
        .asServer(Jetty(port))

fun MyMathApp(recorderHttp: HttpHandler): RoutingHttpHandler {
    val recorder = Recorder(recorderHttp)

    return CatchLensFailure.then(
        routes(
            "/ping" bind GET to { _: Request -> Response(OK) },
            "/add" bind GET to calculate(recorder) { it.sum() },
            "/multiply" bind GET to calculate(recorder) { it.fold(1) { acc, value -> acc * value } },
            singlePageApp(Classpath("mathApp"))
        )
    )
}

private fun calculate(recorder: Recorder, fn: (List<Int>) -> Int): HttpHandler {
    val valuesLens = Query.int().multi.defaulted("value", listOf())
    return { request: Request ->
        val values = valuesLens(request)
        val answer = if (values.isEmpty()) 0 else fn(values)
        recorder.record(answer)
        Response(OK).body(answer.toString())
    }
}

class Recorder(private val client: HttpHandler) {

    fun record(value: Int) {
        client(Request(POST, "/$value"))
    }
}