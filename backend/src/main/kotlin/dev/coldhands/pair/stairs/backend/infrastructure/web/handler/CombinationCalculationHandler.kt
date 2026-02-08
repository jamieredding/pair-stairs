package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.Page
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.combination.ScoredCombination
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorCode
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ScoredCombinationInfo
import dev.coldhands.pair.stairs.core.domain.pairstream.NotEnoughDevelopersException
import dev.coldhands.pair.stairs.core.domain.pairstream.NotEnoughStreamsException
import dev.forkhandles.result4k.*
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

object CombinationCalculationHandler {
    private val queryOptionalPageLens = Query.int().optional("page")
    private val queryOptionalProjectionLens = Query.optional("projection")

    private val postRequestBodyLens = Body.auto<PostCombinationCalculation>().toLens()
    private val defaultProjectionResponseLens = Body.auto<List<ScoredCombinationInfo>>().toLens()
    private val pageProjectionResponseLens = Body.auto<Page<ScoredCombinationInfo>>().toLens()
    private val errorBodyLens = Body.auto<ErrorDto>().toLens()

    data class PostCombinationCalculation(
        val developerIds: List<DeveloperId>,
        val streamIds: List<StreamId>,
    )

    operator fun invoke(
        service: CombinationCalculationService,
        combinationMapper: CombinationMapper
    ): RoutingHttpHandler = routes(
        "/api/v1/combinations/calculate" bind POST to {
            val requestedProjection = queryOptionalProjectionLens(it)

            when (requestedProjection) {
                null -> Projection.DEFAULT.asSuccess()
                "page" -> Projection.PAGE.asSuccess()
                else -> ErrorCode.UNSUPPORTED_PROJECTION.asFailure()
            }
                .mapFailure { errorCode -> errorBodyLens(ErrorDto(errorCode), Response(BAD_REQUEST)) }
                .map { projection ->
                    val page = queryOptionalPageLens(it) ?: 0
                    val requestBody = postRequestBodyLens(it)

                    resultFrom {
                        service.calculate(requestBody.developerIds, requestBody.streamIds, page, 2)
                            .toInfoUsing(combinationMapper)
                    }.mapFailure { exception ->
                        val errorCode = when (exception) {
                            is NotEnoughDevelopersException -> ErrorCode.NOT_ENOUGH_DEVELOPERS
                            is NotEnoughStreamsException -> ErrorCode.NOT_ENOUGH_STREAMS
                            else -> ErrorCode.BAD_REQUEST
                        }
                        errorBodyLens(
                            ErrorDto(errorCode),
                            Response(BAD_REQUEST)
                        )
                    }.map { page ->
                        when (projection) {
                            Projection.DEFAULT -> defaultProjectionResponseLens(page.data, Response(OK))
                            Projection.PAGE -> pageProjectionResponseLens(page, Response(OK))
                        }
                    }.get()
                }
                .get()
        }
    )

    private fun Page<ScoredCombination>.toInfoUsing(combinationMapper: CombinationMapper): Page<ScoredCombinationInfo> {
        return Page(
            metadata = metadata,
            data = data.map { scoredCombination ->
                ScoredCombinationInfo(
                    score = scoredCombination.score,
                    combination = combinationMapper.toInfo(scoredCombination.combination)
                )
            }
        )
    }

    enum class Projection {
        DEFAULT,
        PAGE,
    }

}