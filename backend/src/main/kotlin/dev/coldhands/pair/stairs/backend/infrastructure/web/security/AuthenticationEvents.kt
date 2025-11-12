package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.user.UserName
import dev.coldhands.pair.stairs.backend.usecase.UserDetailsService
import dev.forkhandles.result4k.orThrow
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class AuthenticationEvents(private val userDetailsService: UserDetailsService) {

    @EventListener
    fun onAuthenticationSuccess(event: AuthenticationSuccessEvent) {
        when (val principal = event.authentication.principal) {
            is OidcUser -> {
                val userInfo = principal.userInfo
                val userName = UserName(
                    nickName = userInfo.nickName,
                    givenName = userInfo.givenName,
                    fullName = userInfo.fullName,
                )
                userDetailsService.createOrUpdate(
                    oidcSub = OidcSub(userInfo.subject),
                    userName = userName
                ).orThrow { failure -> error(failure.toString()) }
            }
        }
    }
}