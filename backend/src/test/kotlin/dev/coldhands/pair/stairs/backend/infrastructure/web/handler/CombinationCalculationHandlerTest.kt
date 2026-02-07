package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.ParameterizedJsonApprovalTest
import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.aStreamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.web.testContext
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.testing.Approver
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ParameterizedJsonApprovalTest::class)
class CombinationCalculationHandlerTest {
    private val requestBodyLens = Body.auto<PostCombinationCalculation>().toLens()
    private val queryPageLens = Query.int().required("page")

    @Test
    @Disabled
    fun `when anonymous user then return unauthorized`() {

    }

    @Nested
    inner class Calculate {

        @Test
        fun `calculate combinations has a default page size`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

            val stream0Id = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess().id

            // todo given default page size is 2...

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/combinations/calculate",
                ).with(requestBodyLens of PostCombinationCalculation(
                    developerIds = listOf(dev0Id.value, dev1Id.value, dev2Id.value),
                    streamIds = listOf(stream0Id.value, stream1Id.value)
                ))
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `calculate combinations requesting page with no results`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

            val stream0Id = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess().id

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/combinations/calculate",
                ).with(
                    queryPageLens of 10,

                    requestBodyLens of PostCombinationCalculation(
                    developerIds = listOf(dev0Id.value, dev1Id.value, dev2Id.value),
                    streamIds = listOf(stream0Id.value, stream1Id.value)
                ))
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `calculate combinations returns combinations sorted by stream display name and developers sorted by display name`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

            val stream0Id = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess().id

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/combinations/calculate",
                ).with(
                    requestBodyLens of PostCombinationCalculation(
                        developerIds = listOf(dev0Id.value, dev1Id.value, dev2Id.value),
                        streamIds = listOf(stream0Id.value, stream1Id.value)
                    ))
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }
    }

    @Nested
    inner class CalculateReturningPage {
        private val queryProjectionLens = Query.required("projection")

        @Test
        fun `calculate combinations has a default page size`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

            val stream0Id = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess().id

            // todo given default page size is 2...

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/combinations/calculate",
                ).with(
                    queryProjectionLens of "page",

                    requestBodyLens of PostCombinationCalculation(
                    developerIds = listOf(dev0Id.value, dev1Id.value, dev2Id.value),
                    streamIds = listOf(stream0Id.value, stream1Id.value)
                ))
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `calculate combinations requesting page with no results`(approver: Approver) = testContext {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id

            val stream0Id = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess().id

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/combinations/calculate",
                ).with(
                    queryProjectionLens of "page",
                    queryPageLens of 10,

                    requestBodyLens of PostCombinationCalculation(
                        developerIds = listOf(dev0Id.value, dev1Id.value, dev2Id.value),
                        streamIds = listOf(stream0Id.value, stream1Id.value)
                    ))
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }
    }

    data class PostCombinationCalculation(
        val developerIds: List<Long>,
        val streamIds: List<Long>,
    )

}