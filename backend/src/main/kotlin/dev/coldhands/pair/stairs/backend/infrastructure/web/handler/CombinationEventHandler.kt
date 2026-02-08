package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.GetCombinationEventDto
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

object CombinationEventHandler {
    private val queryOptionalPageLens = Query.int().optional("page")
    private val getEventsResponseLens = Body.auto<List<GetCombinationEventDto>>().toLens()

    operator fun invoke(
        combinationEventService: CombinationEventService,
        combinationMapper: CombinationMapper,
        combinationsEventPageSize: Int,
    ): RoutingHttpHandler = routes(
        "/api/v1/combinations/event" bind GET to {
            val page = queryOptionalPageLens(it) ?: 0

            val results = combinationEventService.getCombinationEvents(page, combinationsEventPageSize)
                .map { event ->
                    GetCombinationEventDto(
                        id = event.id,
                        date = event.date,
                        combination = combinationMapper.toInfo(event.combination)
                    )
                }


            getEventsResponseLens(results, Response(OK))
        }
    )
}