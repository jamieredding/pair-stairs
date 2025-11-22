package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationEventMapper
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.*
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.PairStreamRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate

class CombinationEventService(
    private val developerDao: DeveloperDao,
    private val streamRepository: StreamRepository,
    private val pairStreamRepository: PairStreamRepository,
    private val combinationRepository: CombinationRepository,
    private val combinationEventRepository: CombinationEventRepository,
) {

    fun getCombinationEvents(requestedPage: Int, pageSize: Int): List<CombinationEvent> {
        return combinationEventRepository.findAll(
            PageRequest.of(requestedPage, pageSize, Sort.by(Sort.Direction.DESC, "date")),
        )
            .map { CombinationEventMapper.entityToDomain(it) }
            .toList()
    }

    fun saveEvent(date: LocalDate, combinationByIds: List<SaveCombinationEventDto.PairStreamByIds>) {
        val developerIds = combinationByIds.flatMap { it.developerIds }
        val streamIds = combinationByIds.map { it.streamId }

        val developersById = developerDao.findAllById(developerIds).associateBy { it.id }
        val streamsById = streamRepository.findAllById(streamIds).associateBy { it.id }

        val pairStreamEntities = combinationByIds.map { findOrCreatePairStreamEntity(it, developersById, streamsById) }
        val combination = findOrCreateCombinationEntity(pairStreamEntities)

        combinationEventRepository.save(CombinationEventEntity(date, combination))
    }

    fun deleteEvent(id: Long) {
        if (!combinationEventRepository.existsById(id)) {
            throw EntityNotFoundException()
        }
        combinationEventRepository.deleteById(id)
    }

    private fun findOrCreatePairStreamEntity(
        ids: SaveCombinationEventDto.PairStreamByIds,
        developersById: Map<DeveloperId, Developer>,
        streamsById: Map<Long, StreamEntity>
    ): PairStreamEntity {
        val developerEntities: List<DeveloperEntity> =
            ids.developerIds().map { developersById[it]?.toEntity() ?: error("Developer not found") }
        val streamEntity: StreamEntity = streamsById[ids.streamId()] ?: error("Stream not found")

        return pairStreamRepository.findByDevelopersAndStream(
            ids.developerIds().map { it.value },
            streamEntity,
            ids.developerIds().size.toLong()
        )
            .firstOrNull()
            ?: pairStreamRepository.save(
                PairStreamEntity(
                    developerEntities,
                    streamEntity,
                )
            )
    }

    private fun findOrCreateCombinationEntity(pairStreamEntities: List<PairStreamEntity>): CombinationEntity {
        val pairStreamIds = pairStreamEntities.map { it.id }

        return combinationRepository.findByPairStreams(pairStreamIds, pairStreamIds.size.toLong())
            .firstOrNull()
            ?: combinationRepository.save(CombinationEntity(pairStreamEntities))
    }
}