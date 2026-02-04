package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.Page
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.combination.ScoredCombination
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.LegacyErrorDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ScoredCombinationInfo
import dev.coldhands.pair.stairs.core.domain.pairstream.NotEnoughDevelopersException
import dev.coldhands.pair.stairs.core.domain.pairstream.NotEnoughStreamsException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/combinations/calculate")
class CombinationCalculationController(
    private val service: CombinationCalculationService,
    private val combinationMapper: CombinationMapper,
    @Value($$"${app.combinations.calculate.pageSize}") private val pageSize: Int
) {

    @PostMapping(params = ["projection=page"])
    fun calculateReturningPageProjection(
        @RequestParam("page") page: Int?,
        @RequestBody request: CalculateInputDto,
    ): Page<ScoredCombinationInfo> {
        val requestedPage = page ?: 0
        val developerIds = request.developerIds
        val streamIds = request.streamIds

        val result: Page<ScoredCombinationInfo> =
            service.calculate(developerIds, streamIds, requestedPage, pageSize)
                .toInfo()

        return result
    }

    @PostMapping(params = ["!projection"])
    fun calculateReturningDefaultProjection(
        @RequestParam("page") page: Int?,
        @RequestBody request: CalculateInputDto,
    ): List<ScoredCombinationInfo> {
        val requestedPage = page ?: 0
        val developerIds = request.developerIds
        val streamIds = request.streamIds

        val result: Page<ScoredCombinationInfo> =
            service.calculate(developerIds, streamIds, requestedPage, pageSize)
                .toInfo()

        return result.data
    }

    @ExceptionHandler(NotEnoughDevelopersException::class)
    fun handleNotEnoughDevelopersException(): ResponseEntity<LegacyErrorDto> =
        ResponseEntity.badRequest().body(LegacyErrorDto("NOT_ENOUGH_DEVELOPERS"))

    @ExceptionHandler(NotEnoughStreamsException::class)
    fun handleNotEnoughStreamsException(): ResponseEntity<LegacyErrorDto> =
        ResponseEntity.badRequest().body(LegacyErrorDto("NOT_ENOUGH_STREAMS"))

    fun Page<ScoredCombination>.toInfo(): Page<ScoredCombinationInfo> {
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
