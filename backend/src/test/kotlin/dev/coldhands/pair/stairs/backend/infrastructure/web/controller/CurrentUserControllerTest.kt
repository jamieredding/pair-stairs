package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.anOidcSub
import dev.coldhands.pair.stairs.backend.domain.UserName
import dev.coldhands.pair.stairs.backend.usecase.UserDetailsService
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.util.*
import java.util.stream.Stream

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
open class CurrentUserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val userDetailsService: UserDetailsService,
) {

    @ParameterizedTest
    @MethodSource
    fun whenAnonymousUserThenReturnUnauthorized(httpMethod: HttpMethod, uri: String) {
        mockMvc.perform(
            request(httpMethod, URI.create(uri))
                .with(anonymous())
        ).andExpect(status().isUnauthorized)
    }

    companion object {
        @JvmStatic
        fun whenAnonymousUserThenReturnUnauthorized(): Stream<Arguments> =
            Stream.of(
                Arguments.of(HttpMethod.GET, "/api/v1/me")
            )
    }

    @Nested
    inner class Names {

        @Test
        fun returnADisplayNameForALoggedInUser() {
            val oidcSub = anOidcSub()
            userDetailsService.createOrUpdate(oidcSub, UserName(null, null, "Full Name"))

            mockMvc.perform(
                get("/api/v1/me")
                    .with(
                        oidcLogin().userInfoToken { builder ->
                            builder.name("Full Name")
                                .subject(oidcSub.value)
                        }
                    )
            )
                .andExpect(status().isOk)
                .andExpect(
                    content().json(
                        """
                        {
                          "fullName": "Full Name",
                          "displayName": "Full"
                        }
                        """.trimIndent()
                    )
                )
        }

        @Test
        fun returnNotFoundIfUserIsNotRecognised() {
            val oidcSub = UUID.randomUUID().toString()

            mockMvc.perform(
                get("/api/v1/me")
                    .with(
                        oidcLogin().userInfoToken { builder ->
                            builder.name("Full Name")
                                .subject(oidcSub)
                        }
                    )
            )
                .andExpect(status().isNotFound)
        }

    }
}
