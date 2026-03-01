package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import org.http4k.core.Uri
import kotlin.time.Duration

class OAuthSettings(
    val issuerUri: Uri,
    val jwkUri: Uri,
    val callbackUri: Uri,
    val audience: String,
    val clientId: String,
    val clientSecret: String,
    val loginTokenCookieValidity: Duration,
)
