package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.exceptions.JWTVerificationException
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.resultFromCatching
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.TEMPORARY_REDIRECT
import org.http4k.core.Uri
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidateCookie
import org.http4k.core.with
import org.http4k.lens.Header.LOCATION
import org.http4k.lens.bearerToken
import org.http4k.security.*
import org.http4k.security.openid.IdToken
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaInstant

class InMemoryOAuthPersistence(
    cookieNamePrefix: String,
    private val cookieValidity: Duration = 1.days, // todo should this actually come from the jwt?
    private val clock: Clock,
    private val verifier: JWTVerifier,
    cookieTokenStore: MutableMap<String, AccessToken>
) : OAuthPersistence {
    private val csrfName = "${cookieNamePrefix}Csrf"
    private val nonceName = "${cookieNamePrefix}Nonce"
    private val originalUriName = "${cookieNamePrefix}OriginalUri"
    private val pkceChallengeCookieName = "${cookieNamePrefix}PkceChallenge"
    private val pkceVerifierCookieName = "${cookieNamePrefix}PkceVerifier"
    private val clientAuthCookie = "${cookieNamePrefix}Auth"
    private val cookieSwappableTokens = cookieTokenStore

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
        expires = (clock.now() + cookieValidity).toJavaInstant(),
        path = "/",
//        secure = TODO(),
//        httpOnly = TODO(),
    )

    fun logout(request: Request): Response {
        request.cookie(clientAuthCookie)?.value?.let {
            cookieSwappableTokens.remove(it)
        }
        return Response(TEMPORARY_REDIRECT).with(LOCATION of Uri.of("/"))
            .invalidateCookie(clientAuthCookie)
            .invalidateCookie(csrfName)
            .invalidateCookie(originalUriName)
    }

    fun getAccessToken(request: Request): AccessToken? = tryBearerToken(request) ?: tryCookieToken(request)

}