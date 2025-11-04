package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CurrentUserDto
import dev.coldhands.pair.stairs.backend.usecase.UserDetailsService
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
class CurrentUserController(private val userDetailsService: UserDetailsService) {
    @GetMapping("/api/v1/me")
    fun getCurrentUser(@AuthenticationPrincipal oidcUser: OidcUser): ResponseEntity<CurrentUserDto> {
        val userInfo = oidcUser.userInfo

        return userDetailsService.getUserByOidcSub(OidcSub(userInfo.subject))
            ?.let { ResponseEntity.ok(CurrentUserDto(userInfo.fullName, it.displayName)) }
            ?: ResponseEntity.notFound().build()
    }
}