package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.Page
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.ScoredCombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository

class CoreCombinationCalculationService(
    private val developerDao: DeveloperDao,
    private val streamRepository: StreamRepository,
    private val entryPointFactory: EntryPointFactory
) : CombinationCalculationService {
    override fun calculate(
        developerIds: List<DeveloperId>,
        streamIds: List<Long>,
        page: Int,
        pageSize: Int
    ): Page<ScoredCombination> {
        // todo validate that ids are real?

        val entryPoint = entryPointFactory.create(
            developerIds.map { it.value.toString() },
            streamIds.map { it.toString() }
        )

        val scoredCombinations = entryPoint.computeScoredCombinations()

        val developerLookup = developerDao.findAllById(developerIds).associateBy { it.id }
        val streamLookup = streamRepository.findAllById(streamIds).associateBy { it.id }

        val pages = scoredCombinations.windowed(size = pageSize, step = pageSize, partialWindows = true)

        if (page < 0 || page >= pages.size) {
            return Page.empty()
        }

        return Page(
            metadata = Page.Metadata(
                nextPageNumber = (page + 1).takeIf { it < pages.size },
            ),
            data = pages
                .drop(page)
                .first()
                .map { sc ->
                    ScoredCombinationMapper.coreToDomain(
                        sc,
                        { id -> developerLookup[DeveloperId(id)]?.toEntity() ?: error("Developer not found") },
                        streamLookup::get
                    )
                }
        )
    }
}