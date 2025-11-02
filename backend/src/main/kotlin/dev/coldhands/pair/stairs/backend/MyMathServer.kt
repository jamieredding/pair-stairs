package dev.coldhands.pair.stairs.backend

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.resultFromCatching
import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidateCookie
import org.http4k.filter.ClientFilters.SetHostFrom
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters.CatchLensFailure
import org.http4k.lens.*
import org.http4k.lens.Header.LOCATION
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
import java.net.URI
import java.security.interfaces.RSAPublicKey
import java.time.Clock
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

fun main() {
    MyMathServer(8080, Uri.of("http://localhost:18082")).start()
}

data class OidcUser(val userInfo: UserInfo) {
    data class UserInfo(
        val subject: String,
        val nickName: String?,
        val givenName: String?,
        val fullName: String?,
    )
}

val oidcUserLens = RequestKey.required<OidcUser>("oidcUser")

fun MyMathServer(port: Int, recorderUri: Uri): Http4kServer {
    val app = MyMathApp(recorderHttp = SetHostFrom(recorderUri).then(OkHttp()))
    val callbackUri = Uri.of("http://localhost:8080/login/oauth2/code/oauth")
    val dexIdpBase = Uri.of("http://localhost:5556")

    val verifier = jWTVerifier(
        jwkUri = dexIdpBase.appendToPath("/dex/keys"),
        issuer = dexIdpBase.appendToPath("/dex").toString(),
        audience = "pair-stairs",
    )

    val oAuthPersistence = InMemoryOAuthPersistence(
        cookieNamePrefix = "pair-stairs",
        verifier = verifier,
    )
    val oauthProvider = OAuthProvider(
        providerConfig = OAuthProviderConfig(
            authBase = dexIdpBase,
            authPath = "/dex/auth",
            tokenPath = "/dex/token",
            credentials = Credentials("pair-stairs", "ZXhhbXBsZS1hcHAtc2VjcmV0"),
        ),
        client = OkHttp(),
        callbackUri = callbackUri,
        scopes = listOf("openid", "profile", "email"), // unsure about these
        oAuthPersistence = oAuthPersistence, // this needs to be implemented as in https://www.http4k.org/howto/use_a_custom_oauth_provider/
    )

    fun assignUserPrincipal(key: RequestLens<OidcUser>) = Filter { next ->
        { request ->
            oAuthPersistence.getAccessToken(request)
                ?.value
                ?.let { token -> decodeFromToken(token) }
                ?.let { oidcUser ->
                    next(request.with(key of oidcUser))
                }
                ?: Response(UNAUTHORIZED)
        }
    }

    val secureApp: HttpHandler =
        DebuggingFilters.PrintRequestAndResponse()
            .then(
                routes(
                    "/logout" bind { request -> oAuthPersistence.logout(request) },
                    oauthProvider.callbackEndpoint,
                    oauthProvider.authFilter
                        .then(assignUserPrincipal(oidcUserLens))
                        .then(app)
                )
            )


    return secureApp
        .asServer(Jetty(port))
}

private fun jWTVerifier(jwkUri: Uri, issuer: String, audience: String): JWTVerifier {
    val jwkProvider = JwkProviderBuilder(URI.create(jwkUri.toString()).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val rsaKeyProvider = object : RSAKeyProvider {
        override fun getPublicKeyById(keyId: String) = jwkProvider.get(keyId).publicKey as RSAPublicKey

        override fun getPrivateKey() = null

        override fun getPrivateKeyId() = null
    }

    return JWT.require(Algorithm.RSA256(rsaKeyProvider))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
}

fun decodeFromToken(token: String): OidcUser? {
    // decode only here as verification happens elsewhere
    val result = resultFromCatching<JWTDecodeException, DecodedJWT> { JWT.decode(token) }
        .map {
            OidcUser(
                userInfo = OidcUser.UserInfo(
                    subject = it.subject,
                    nickName = it.claims["nickname"]?.asString(),
                    givenName = it.claims["given_name"]?.asString(),
                    fullName = it.claims["name"]?.asString(),
                )
            )
        }
    return when (result) {
        is Success<OidcUser> -> result.value
        is Failure<*> -> null // todo failure is ignored and not logged
    }
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
    private val cookieValidity: Duration = Duration.ofDays(1), // todo should this actually come from the jwt?
    private val clock: Clock = Clock.systemUTC(),
    private val verifier: JWTVerifier
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

    override fun retrieveCsrf(request: Request): CrossSiteRequestForgeryToken? =
        request.cookie(csrfName)?.value?.let(::CrossSiteRequestForgeryToken)

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

    override fun retrieveToken(request: Request): AccessToken? = getAccessToken(request)
        ?.takeIf {
            when (resultFromCatching<JWTVerificationException, Any> { verifier.verify(it.value) }) {
                is Success<*> -> true
                is Failure<*> -> false // todo ignoring the failure reason
            }
        }

    private fun tryBearerToken(request: Request): AccessToken? =
        request.bearerToken()?.let { AccessToken(it) }

    private fun tryCookieToken(request: Request): AccessToken? =
        request.cookie(clientAuthCookie)?.value?.let { cookieSwappableTokens[it] }

    private fun expiring(name: String, value: String) = Cookie(
        name = name,
        value = value,
        expires = clock.instant().plus(cookieValidity),
        path = "/",
//        secure = TODO(),
//        httpOnly = TODO(),
    )

    fun logout(request: Request): Response {
        request.cookie(clientAuthCookie)?.value?.let {
            cookieSwappableTokens.remove(it)
        }
        return Response(Status.TEMPORARY_REDIRECT).with(LOCATION of Uri.of("/"))
            .invalidateCookie(clientAuthCookie)
            .invalidateCookie(csrfName)
            .invalidateCookie(originalUriName)
    }

    fun getAccessToken(request: Request): AccessToken? = tryBearerToken(request) ?: tryCookieToken(request)

}