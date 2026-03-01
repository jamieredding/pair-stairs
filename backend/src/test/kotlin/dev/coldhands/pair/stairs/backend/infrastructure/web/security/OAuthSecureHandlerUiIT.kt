package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole.*
import dev.coldhands.pair.stairs.backend.infrastructure.web.TestContext
import io.kotest.matchers.url.haveRef
import org.http4k.playwright.Http4kBrowser
import org.http4k.playwright.LaunchPlaywrightBrowser
import org.http4k.server.SunHttp
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class OAuthSecureHandlerUiIT {

    private val testContext = TestContext()

    @RegisterExtension
    private val playwright = LaunchPlaywrightBrowser(
        http = testContext.underTest,
        serverFn = { SunHttp(8080) },
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