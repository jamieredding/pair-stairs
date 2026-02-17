package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.infrastructure.Settings
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.web.security.InMemoryOAuthPersistence
import dev.coldhands.pair.stairs.backend.infrastructure.web.security.jwtVerifier
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import dev.coldhands.pair.stairs.backend.usecase.StatsService
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.resultFromCatching
import org.http4k.core.*
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.filter.DebuggingFilters
import org.http4k.lens.RequestKey
import org.http4k.lens.RequestLens
import org.http4k.routing.*
import org.http4k.security.AccessToken
import org.http4k.security.OAuthProvider
import org.http4k.security.OAuthProviderConfig
import kotlin.time.Clock

class AppHttpHandler(
    developerDao: DeveloperDao,
    streamDao: StreamDao,
    teamDao: TeamDao,
    statsService: StatsService,
    combinationCalculationService: CombinationCalculationService,
    combinationEventService: CombinationEventService,
    combinationMapper: CombinationMapper,
    oAuthClient: HttpHandler,
    clock: Clock,
    settings: Settings,
    cookieTokenStore: MutableMap<String, AccessToken>,
) : HttpHandler {

    private val verifier = jwtVerifier(
        jwkUri = settings.oauthJwkUri,
        issuer = settings.oauthIssuerUri,
        audience = settings.oauthAudience
    )

    private val oAuthPersistence = InMemoryOAuthPersistence(
        cookieNamePrefix = "pair-stairs",
        cookieValidity = settings.loginTokenCookieValidity,
        clock = clock,
        verifier = verifier,
        cookieTokenStore = cookieTokenStore
    )

    private val oAuthProvider = OAuthProvider(
        providerConfig = OAuthProviderConfig(
            authBase = settings.oauthIssuerUri,
            authPath = "/auth",
            tokenPath = "/token",
            credentials = Credentials(settings.oauthClientId, settings.oauthClientSecret),
        ),
        client = oAuthClient,
        callbackUri = settings.oauthCallbackUri,
        scopes = listOf("openid", "profile", "email"), // todo unsure about these
        oAuthPersistence = oAuthPersistence
    )

    private val apiRoutes = routes(
        DeveloperHandler(developerDao, statsService),
        StreamHandler(streamDao, statsService),
        CombinationCalculationHandler(
            combinationCalculationService,
            combinationMapper,
            settings.combinationsCalculatePageSize
        ),
        CombinationEventHandler(combinationEventService, combinationMapper, settings.combinationsEventPageSize),
        TeamHandler(teamDao)
    )
    data class OidcUser(val userInfo: UserInfo) {
        data class UserInfo(
            val subject: String,
            val nickName: String?,
            val givenName: String?,
            val fullName: String?,
        )
    }

    val oidcUserLens = RequestKey.required<OidcUser>("oidcUser")

    private val appStack: RoutingHttpHandler = CatchLensFailureFilter()
        .then(
            routes(
                apiRoutes,
                singlePageApp(ResourceLoader.Directory(settings.staticContentPath.toFile().absolutePath))
            )
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

    private val secureApp: HttpHandler = DebuggingFilters.PrintRequestAndResponse()
        .then(
            routes(
                "/logout" bind {request -> oAuthPersistence.logout(request)},
                oAuthProvider.callbackEndpoint,
                oAuthProvider.authFilter
                    .then(assignUserPrincipal(oidcUserLens))
                    .then(
                        appStack
                    )
            )
        )

    override fun invoke(request: Request): Response = secureApp(request)

}