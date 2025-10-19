package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.ScoredCombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository

class CoreCombinationCalculationService(
    private val developerRepository: DeveloperRepository,
    private val streamRepository: StreamRepository,
    private val entryPointFactory: EntryPointFactory
) : CombinationCalculationService {
    override fun calculate(
        developerIds: List<Long>,
        streamIds: List<Long>,
        page: Int,
        pageSize: Int
    ): List<ScoredCombination> {
        // todo validate that ids are real?

        val entryPoint = entryPointFactory.create(
            asStrings(developerIds),
            asStrings(streamIds)
        )

        val scoredCombinations = entryPoint.computeScoredCombinations()

        val developerLookup = developerRepository.findAllById(developerIds).associateBy { it.id }
        val streamLookup = streamRepository.findAllById(streamIds).associateBy { it.id }

        return scoredCombinations.drop(page * pageSize)
            .take(pageSize)
            .map { sc -> ScoredCombinationMapper.coreToDomain(sc, developerLookup::get, streamLookup::get) }
    }

    companion object {
        private fun asStrings(ids: List<Long>): List<String> {
            return ids.map(Long::toString)
        }
    }
}