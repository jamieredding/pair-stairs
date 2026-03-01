package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import org.http4k.lens.RequestKey

data class OidcUser(val userInfo: UserInfo) {
    data class UserInfo(
        val subject: String,
        val nickName: String?,
        val givenName: String?,
        val fullName: String?,
    )
}

val oidcUserLens = RequestKey.required<OidcUser>("oidcUser")