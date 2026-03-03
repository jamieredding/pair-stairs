package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole.*
import io.kotest.matchers.url.haveRef
import org.http4k.client.JavaHttpClient
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.appendToPath
import org.http4k.playwright.Http4kBrowser
import org.http4k.playwright.LaunchPlaywrightBrowser
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.singlePageApp
import org.http4k.security.AccessToken
import org.http4k.server.SunHttp
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class OAuthSecureHandlerUiIT {

    private val appBaseUri = Uri.of("http://localhost:8080")
    private val oAuthSettings = OAuthSettings(
        issuerUri = Uri.of("http://localhost:5556/dex"),
        jwkUri = Uri.of("http://localhost:5556/dex/keys"),
        callbackUri = appBaseUri.appendToPath("/login/oauth2/code/oauth"),
        audience = "pair-stairs",
        clientId = "pair-stairs",
        clientSecret = "ZXhhbXBsZS1hcHAtc2VjcmV0",
        loginTokenCookieValidity = 1.minutes
    )
    private val cookieTokenStore = mutableMapOf<String, AccessToken>()
    private val underTest = OAuthSecureHandler(
        routes = OAuthSecureHandler.Routes(
            apiRoutes = "/api/test" bind Method.GET to { Response(OK) },
            authFilteredRoutes = singlePageApp(ResourceLoader.Classpath("static-test")),
        ),
        oAuthSettings = oAuthSettings,
        oAuthClient = JavaHttpClient(),
        clock = Clock.System,
        cookieTokenStore = cookieTokenStore,
    )

    @RegisterExtension
    private val playwright = LaunchPlaywrightBrowser(
        http = underTest,
        serverFn = { SunHttp(appBaseUri.port!!) },
        launchOptions = BrowserType.LaunchOptions().apply {
//            headless = false
//            slowMo = 500.milliseconds.inWholeMilliseconds.toDouble()
        }
    )

    @Test
    // todo ensure playwright is setup on server/build is run in a container that supports playwright
    fun `can perform browser auth flow`(browser: Http4kBrowser) {
        with(browser.newPage()) {
            navigate("/")

            assertThat(
                getByRole(
                    HEADING,
                    Page.GetByRoleOptions().apply { name = "Log in to Your Account" })
            )
                .isVisible()

            getByPlaceholder("email address").apply {
                click()
                fill("admin@example.com")
            }

            getByPlaceholder("password").apply {
                click()
                fill("password")
            }

            getByRole(BUTTON, Page.GetByRoleOptions().apply { name = "Login" })
                .click()

            getByRole(BUTTON, Page.GetByRoleOptions().apply { name = "Grant Access" })
                .click()

            assertThat(
                getByRole(
                    HEADING,
                    Page.GetByRoleOptions().apply { name = "This is a test index page" })
            )
                .isVisible()

            getByRole(LINK, Page.GetByRoleOptions().apply { haveRef("/logout") })
                .click()

            assertThat(
                getByRole(
                    HEADING,
                    Page.GetByRoleOptions().apply { name = "Log in to Your Account" })
            )
                .isVisible()
        }
    }
}