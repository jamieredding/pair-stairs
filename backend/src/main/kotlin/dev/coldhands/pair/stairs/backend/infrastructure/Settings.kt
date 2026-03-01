package dev.coldhands.pair.stairs.backend.infrastructure

import dev.coldhands.pair.stairs.backend.infrastructure.web.security.OAuthSettings
import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.lens.uri
import java.nio.file.Path
import kotlin.time.Duration

class Settings(environment: Environment) {

    val combinationsCalculatePageSize: Int =
        EnvironmentKey.int().required("app.combinations.calculate.pageSize")(environment)
    val combinationsEventPageSize: Int = EnvironmentKey.int().required("app.combinations.event.pageSize")(environment)

    val staticContentPath: Path = EnvironmentKey.map { Path.of(it) }.required("static.content.path")(environment)

    // todo do I need a different jwt issuer uri?
    // todo should all of these be optional?
    val oauth: OAuthSettings = OAuthSettings(
        issuerUri = EnvironmentKey.uri().required("oauth.issuer.uri")(environment),
        jwkUri = EnvironmentKey.uri().required("oauth.jwk.uri")(environment),
        callbackUri = EnvironmentKey.uri().required("oauth.callback.uri")(environment),
        audience = EnvironmentKey.required("oauth.audience")(environment),
        clientId = EnvironmentKey.string().required("oauth.client.id")(environment),
        clientSecret = EnvironmentKey.string().required("oauth.client.secret")(environment),
        loginTokenCookieValidity = EnvironmentKey.map { Duration.parse(it) }.required("login.token.cookie.validity")(environment)
    )
}