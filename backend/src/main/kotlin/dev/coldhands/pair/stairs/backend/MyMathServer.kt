package dev.coldhands.pair.stairs.backend

import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ClientFilters.SetHostFrom
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters.CatchLensFailure
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.security.InsecureCookieBasedOAuthPersistence
import org.http4k.security.OAuthProvider
import org.http4k.security.OAuthProviderConfig
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    MyMathServer(8080, Uri.of("http://localhost:18082")).start()
}

fun MyMathServer(port: Int, recorderUri: Uri): Http4kServer {
    val app = MyMathApp(recorderHttp = SetHostFrom(recorderUri).then(OkHttp()))
    val callbackUri = Uri.of("http://localhost:8080/login/oauth2/code/oauth")

    val oauthProvider = OAuthProvider(
        providerConfig = OAuthProviderConfig(
            authBase = Uri.of("http://localhost:5556"),
            authPath = "/dex/auth",
            tokenPath = "/dex/token",
            credentials = Credentials("pair-stairs", "ZXhhbXBsZS1hcHAtc2VjcmV0"),
        ),
        client = OkHttp(),
        callbackUri = callbackUri,
        scopes = listOf("openid", "profile", "email"), // unsure about these
        oAuthPersistence = InsecureCookieBasedOAuthPersistence("pair-stairs"), // this needs to be implemented as in https://www.http4k.org/howto/use_a_custom_oauth_provider/
    )

    val secureApp: HttpHandler =
        DebuggingFilters.PrintRequestAndResponse()
            .then(
                routes(
                    oauthProvider.callbackEndpoint,
                    oauthProvider.authFilter.then(app)
                )
            )


    return secureApp
        .asServer(Jetty(port))
}

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