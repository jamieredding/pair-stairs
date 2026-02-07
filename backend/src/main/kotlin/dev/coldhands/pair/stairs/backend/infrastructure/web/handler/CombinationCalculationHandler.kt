package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.Page
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.combination.ScoredCombination
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ScoredCombinationInfo
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

object CombinationCalculationHandler {
    private val queryOptionalPageLens = Query.int().optional("page")

    private val postRequestBodyLens = Body.auto<PostCombinationCalculation>().toLens()
    private val defaultResponseLens = Body.auto<List<ScoredCombinationInfo>>().toLens()

    data class PostCombinationCalculation(
        val developerIds: List<DeveloperId>,
        val streamIds: List<StreamId>,
    )

    operator fun invoke(
        service: CombinationCalculationService,
        combinationMapper: CombinationMapper
    ): RoutingHttpHandler = routes(
        "/api/v1/combinations/calculate" bind POST to {
            val page = queryOptionalPageLens(it) ?: 0
            val requestBody = postRequestBodyLens(it)

            val results = service.calculate(requestBody.developerIds, requestBody.streamIds, page, 2)
                .toInfoUsing(combinationMapper)

            Response(OK)
                .with(defaultResponseLens of results.data)
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

}