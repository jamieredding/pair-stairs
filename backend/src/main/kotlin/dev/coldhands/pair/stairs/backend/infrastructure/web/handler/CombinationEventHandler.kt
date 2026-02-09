package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateCombinationEventDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.GetCombinationEventDto
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService.CombinationEventNotFound
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.core.Body
import org.http4k.core.Method.*
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

object CombinationEventHandler {
    private val queryOptionalPageLens = Query.int().optional("page")
    private val pathCombinationEventIdLens = Path.map { CombinationEventId(it.toLong()) }.of("id")
    private val getEventsResponseLens = Body.auto<List<GetCombinationEventDto>>().toLens()
    private val createCombinationEventDto = Body.auto<CreateCombinationEventDto>().toLens()

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
        },

        "/api/v1/combinations/event" bind POST to {
            val requestBody = createCombinationEventDto(it)

            // todo HTTP4K-MIGRATION what about return of this? it could have failed
            combinationEventService.saveEvent(requestBody.date, requestBody.combination)

            Response(CREATED)
        },

        "/api/v1/combinations/event/{id}" bind DELETE to { request ->
            val combinationEventId = pathCombinationEventIdLens(request)

            combinationEventService.deleteEvent(combinationEventId)
                .map { Response(NO_CONTENT) }
                .mapFailure {
                    when (it) {
                        is CombinationEventNotFound -> Response(NOT_FOUND)
                    }
                }.get()
        }
    )
}