package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.core.domain.pairstream.NotEnoughDevelopersException
import dev.coldhands.pair.stairs.core.domain.pairstream.NotEnoughStreamsException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/combinations/calculate")
class CombinationCalculationController(
    private val service: CombinationCalculationService,
    @Value($$"${app.combinations.calculate.pageSize}") private val pageSize: Int
) {

    @PostMapping
    fun calculate(
        @RequestParam("page") page: Int? = 0,
        @RequestParam("projection") projection: SupportedProjection?,
        @RequestBody request: CalculateInputDto,
    ): Any {
        val requestedPage = page ?: 0
        val developerIds = request.developerIds
        val streamIds = request.streamIds

        val result = service.calculate(developerIds, streamIds, requestedPage, pageSize)

        return when(projection) {
            SupportedProjection.PAGE -> result
            null -> result.data
        }
    }

    @ExceptionHandler(NotEnoughDevelopersException::class)
    fun handleNotEnoughDevelopersException(): ResponseEntity<ErrorDto> =
        ResponseEntity.badRequest().body(ErrorDto("NOT_ENOUGH_DEVELOPERS"))

    @ExceptionHandler(NotEnoughStreamsException::class)
    fun handleNotEnoughStreamsException(): ResponseEntity<ErrorDto> =
        ResponseEntity.badRequest().body(ErrorDto("NOT_ENOUGH_STREAMS"))

    companion object {
        enum class SupportedProjection {
            PAGE
        }
    }
}
