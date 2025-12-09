package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.Page
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.combination.ScoredCombination
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.PairStreamInfo
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
    private val developerDao: DeveloperDao,
    private val streamDao: StreamDao,
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

        val result: Page<ScoredCombinationInfo> = service.calculate(developerIds, streamIds, requestedPage, pageSize)
            .toInfo(
                knownDeveloperIds = developerIds,
                knownStreamIds = streamIds
            )

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

        val result: Page<ScoredCombinationInfo> = service.calculate(developerIds, streamIds, requestedPage, pageSize)
            .toInfo(
                knownDeveloperIds = developerIds,
                knownStreamIds = streamIds
            )

        return result.data
    }

    @ExceptionHandler(NotEnoughDevelopersException::class)
    fun handleNotEnoughDevelopersException(): ResponseEntity<ErrorDto> =
        ResponseEntity.badRequest().body(ErrorDto("NOT_ENOUGH_DEVELOPERS"))

    @ExceptionHandler(NotEnoughStreamsException::class)
    fun handleNotEnoughStreamsException(): ResponseEntity<ErrorDto> =
        ResponseEntity.badRequest().body(ErrorDto("NOT_ENOUGH_STREAMS"))

    fun Page<ScoredCombination>.toInfo(
        knownDeveloperIds: List<DeveloperId>,
        knownStreamIds: List<StreamId>
    ): Page<ScoredCombinationInfo> {
        val developers = developerDao.findAllById(knownDeveloperIds)
        val streams = streamDao.findAllById(knownStreamIds)
        return Page(
            metadata = metadata,
            data = data.map { scoredCombination ->
                ScoredCombinationInfo(
                    score = scoredCombination.score,
                    combination = scoredCombination.combination.map { pairStream ->
                        PairStreamInfo(
                            developers = pairStream.developerIds.map { developerId ->
                                developers.firstOrNull { it.id == developerId }?.toInfo()
                                    ?: error("bad times")
                            }.sorted(),
                            stream = streams.firstOrNull { it.id == pairStream.streamId }?.toInfo()
                                ?: error("bad times")
                        )
                    }.sorted()
                )
            }
        )
    }
}
