package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeDeveloperDao
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.kotest.shouldHaveStatus
import org.http4k.testing.Approver
import org.http4k.testing.JsonApprovalTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(JsonApprovalTest::class)
class DeveloperHandlerTest {

    private val developerDao = FakeDeveloperDao()
    private val underTest = DeveloperHandler(developerDao)

    @Test
    @Disabled
    fun `when anonymous user then return unauthorized`() {

    }

    @Nested
    inner class Read {

        @Test
        fun `when no developers then return empty array`(approver: Approver) {
            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple developers then them`(approver: Approver) {
            developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess()
            developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers",
                )
            )

            response shouldHaveStatus OK

            approver.assertApproved(response)
        }
    }

    @Nested
    inner class ReadDeveloperInfo {

        @Test
        fun `when no developers then return empty array`(approver: Approver) {
            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers/info",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple developers then them`(approver: Approver) {
            developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess()
            developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/developers/info",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }
    }

    @Nested
    inner class Write {

        @Test
        fun `save a developer`(approver: Approver) {
            val requestBodyLens = Body.auto<PostDeveloper>().toLens()
            val responseBodyLens = Body.auto<Developer>().toLens()

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/developers",
                ).with(requestBodyLens of PostDeveloper("dev-0"))
            )

            response shouldHaveStatus CREATED
            approver.assertApproved(response)

            val createdDeveloper = responseBodyLens(response)

            developerDao.findById(createdDeveloper.id).shouldNotBeNull {
                id shouldBe createdDeveloper.id
                name shouldBe "dev-0"
                archived shouldBe false
            }
        }
    }

    // todo patch, read developer stats

    data class PostDeveloper(
        val name: String,
    )

}