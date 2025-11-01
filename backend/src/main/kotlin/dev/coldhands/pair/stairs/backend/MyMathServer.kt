package dev.coldhands.pair.stairs.backend

import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidateCookie
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
import org.http4k.security.*
import org.http4k.security.openid.IdToken
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.time.Clock
import java.time.Duration
import java.util.*

fun main() {
    MyMathServer(8080, Uri.of("http://localhost:18082")).start()
}

fun MyMathServer(port: Int, recorderUri: Uri): Http4kServer {
    val app = MyMathApp(recorderHttp = SetHostFrom(recorderUri).then(OkHttp()))
    val callbackUri = Uri.of("http://localhost:8080/login/oauth2/code/oauth")

    val oAuthPersistence = InMemoryOAuthPersistence("pair-stairs")
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
        oAuthPersistence = oAuthPersistence, // this needs to be implemented as in https://www.http4k.org/howto/use_a_custom_oauth_provider/
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

class InMemoryOAuthPersistence(
    cookieNamePrefix: String,
    private val cookieValidity: Duration = Duration.ofDays(1),
    private val clock: Clock = Clock.systemUTC()
) : OAuthPersistence {
    private val csrfName = "${cookieNamePrefix}Csrf"
    private val nonceName = "${cookieNamePrefix}Nonce"
    private val originalUriName = "${cookieNamePrefix}OriginalUri"
    private val pkceChallengeCookieName = "${cookieNamePrefix}PkceChallenge"
    private val pkceVerifierCookieName = "${cookieNamePrefix}PkceVerifier"
    private val clientAuthCookie = "${cookieNamePrefix}Auth"

    private val cookieSwappableTokens = mutableMapOf<String, AccessToken>()

    override fun assignCsrf(
        redirect: Response,
        csrf: CrossSiteRequestForgeryToken
    ): Response = redirect.cookie(expiring(csrfName, csrf.value))

    override fun retrieveCsrf(request: Request): CrossSiteRequestForgeryToken? = request.cookie(csrfName)?.value?.let(::CrossSiteRequestForgeryToken)

    override fun assignNonce(
        redirect: Response,
        nonce: Nonce
    ): Response = redirect.cookie(expiring(nonceName, nonce.value))

    override fun retrieveNonce(request: Request): Nonce? = request.cookie(nonceName)?.value?.let(::Nonce)

    override fun assignOriginalUri(
        redirect: Response,
        originalUri: Uri
    ): Response = redirect.cookie(expiring(originalUriName, originalUri.toString()))

    override fun retrieveOriginalUri(request: Request): Uri? = request.cookie(originalUriName)?.value?.let(Uri::of)

    override fun assignPkce(
        redirect: Response,
        pkce: PkceChallengeAndVerifier
    ): Response = redirect.cookie(expiring(pkceChallengeCookieName, pkce.challenge))
        .cookie(expiring(pkceVerifierCookieName, pkce.verifier))

    override fun retrievePkce(request: Request): PkceChallengeAndVerifier? {
        return PkceChallengeAndVerifier(
            challenge = request.cookie(pkceChallengeCookieName)?.value ?: return null,
            verifier = request.cookie(pkceVerifierCookieName)?.value ?: return null,
        )
    }

    override fun assignToken(
        request: Request,
        redirect: Response,
        accessToken: AccessToken,
        idToken: IdToken?
    ): Response = UUID.randomUUID().toString().let {
        cookieSwappableTokens[it] = accessToken
        redirect.cookie(expiring(clientAuthCookie, it))
            .invalidateCookie(csrfName)
            .invalidateCookie(originalUriName)
    }

    override fun retrieveToken(request: Request): AccessToken? = (/* tryBearerToken ?: */ tryCookieToken(request))
        ?.takeIf {
            // todo verify token (not expired, is for me, all sorts of other things, see video
            true
        }

    private fun tryCookieToken(request: Request) = request.cookie(clientAuthCookie)?.value?.let { cookieSwappableTokens[it]}

    private fun expiring(name: String, value:String) = Cookie(
        name = name,
        value = value,
        expires = clock.instant().plus(cookieValidity),
        path = "/",
//        secure = TODO(),
//        httpOnly = TODO(),
    )

}