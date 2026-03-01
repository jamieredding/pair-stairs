package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import dev.coldhands.pair.stairs.backend.infrastructure.web.handler.CatchLensFailureFilter
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.resultFromCatching
import org.http4k.core.*
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.lens.RequestLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.security.AccessToken
import org.http4k.security.OAuthProvider
import org.http4k.security.OAuthProviderConfig
import kotlin.time.Clock

class OAuthSecureHandler(
    routes: Routes,
    oAuthSettings: OAuthSettings,
    oAuthClient: HttpHandler,
    clock: Clock,
    cookieTokenStore: MutableMap<String, AccessToken>
) : HttpHandler {

    private val verifier = jwtVerifier(
        jwkUri = oAuthSettings.jwkUri,
        issuer = oAuthSettings.issuerUri,
        audience = oAuthSettings.audience,
        client = oAuthClient
    )

    private val oAuthPersistence = InMemoryOAuthPersistence(
        cookieNamePrefix = "pair-stairs",
        cookieValidity = oAuthSettings.loginTokenCookieValidity,
        clock = clock,
        verifier = verifier,
        cookieTokenStore = cookieTokenStore
    )

    private val oAuthProvider = OAuthProvider(
        providerConfig = OAuthProviderConfig(
            authBase = oAuthSettings.issuerUri,
            authPath = "/auth",
            tokenPath = "/token",
            credentials = Credentials(oAuthSettings.clientId, oAuthSettings.clientSecret),
        ),
        client = oAuthClient,
        callbackUri = oAuthSettings.callbackUri,
        scopes = listOf("openid", "profile", "email"), // todo unsure about these
        oAuthPersistence = oAuthPersistence
    )

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

    private fun assignUserPrincipal(key: RequestLens<OidcUser>) = Filter { next ->
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

    private val secureApp: HttpHandler = routes(
        "/logout" bind { request -> oAuthPersistence.logout(request) },
        oAuthProvider.callbackEndpoint,
        Filter { next ->
            { request ->
                oAuthPersistence.getAccessToken(request)
                    ?.takeIf {
                        when (resultFromCatching<JWTVerificationException, Any> { verifier.verify(it.value) }) {
                            is Success<*> -> true
                            is Failure<*> -> false // todo ignoring the failure reason
                        }
                    }
                    ?.let { next(request) }
                    ?: Response(UNAUTHORIZED)
            }
        }
            .then(CatchLensFailureFilter())
            .then(assignUserPrincipal(oidcUserLens))
            .then(routes.apiRoutes),
        oAuthProvider.authFilter
            .then(
                routes.authFilteredRoutes
            )
    )

    override fun invoke(request: Request): Response = secureApp(request)

    class Routes(
        val apiRoutes: RoutingHttpHandler,
        val authFilteredRoutes: RoutingHttpHandler,
    )
}

