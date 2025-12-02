package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.PageRequest
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventCreateError
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDao
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDetails
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.PairStreamRepository
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import org.springframework.data.domain.Sort
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

class JpaCombinationEventDao(
    private val combinationEventRepository: CombinationEventRepository,
    private val combinationRepository: CombinationRepository,
    private val pairStreamRepository: PairStreamRepository,
    private val developerDao: DeveloperDao,
    private val streamDao: StreamDao,
) : CombinationEventDao {
    override fun findById(combinationEventId: CombinationEventId): CombinationEvent? =
        combinationEventRepository.findById(combinationEventId.value).getOrNull()
            ?.toDomain()

    override fun findAll(pageRequest: PageRequest<CombinationEventDao.FindAllSort>): List<CombinationEvent> {
        return combinationEventRepository.findAll(
            org.springframework.data.domain.PageRequest.of(
                pageRequest.requestedPage,
                pageRequest.pageSize,
                when (pageRequest.sort) {
                    CombinationEventDao.FindAllSort.DATE_DESCENDING -> Sort.Direction.DESC
                },
                "date"
            )
        )
            .map { it.toDomain() }
            .toList()
    }

    override fun findByDeveloperId(developerId: DeveloperId): List<CombinationEvent> =
        combinationEventRepository.findByDeveloperId(developerId.value)
            .map { it.toDomain() }

    override fun findByDeveloperIdBetween(
        developerId: DeveloperId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CombinationEvent> =
        combinationEventRepository.findByDeveloperIdBetween(
            developerId = developerId.value,
            startDate = startDate,
            endDate = endDate
        ).map { it.toDomain() }

    override fun findByStreamId(streamId: StreamId): List<CombinationEvent> =
        combinationEventRepository.findByStreamId(streamId.value)
            .map { it.toDomain() }

    override fun findByStreamIdBetween(
        streamId: StreamId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CombinationEvent> =
        combinationEventRepository.findByStreamIdBetween(
            streamId = streamId.value,
            startDate = startDate,
            endDate = endDate
        ).map { it.toDomain() }

    override fun create(combinationEventDetails: CombinationEventDetails): Result<CombinationEvent, CombinationEventCreateError> {
        val pairStreamEntities: List<PairStreamEntity> = combinationEventDetails.combination.map { pairStream ->
            pairStreamRepository.save(
                PairStreamEntity(
                    developers = pairStream.developerIds.map { developerId ->
                        developerDao.findById(developerId)?.toEntity()
                            ?: return CombinationEventCreateError.DeveloperNotFound(developerId).asFailure()
                    },
                    stream = streamDao.findById(pairStream.streamId)?.toEntity()
                        ?: return CombinationEventCreateError.StreamNotFound(pairStream.streamId).asFailure()
                )
            )
        }
        val combinationEntity: CombinationEntity = combinationRepository.save(
            CombinationEntity(
                pairs = pairStreamEntities
            )
        )

        return combinationEventRepository.save(
            CombinationEventEntity(
                date = combinationEventDetails.date,
                combination = combinationEntity
            )
        ).toDomain().asSuccess()
    }

    override fun delete(combinationEventId: CombinationEventId) {
        combinationEventRepository.deleteById(combinationEventId.value)
    }
}