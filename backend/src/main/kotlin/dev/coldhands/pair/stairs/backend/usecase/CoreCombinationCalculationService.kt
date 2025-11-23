package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.ScoredCombinationMapper
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toEntity

class CoreCombinationCalculationService(
    private val developerDao: DeveloperDao,
    private val streamDao: StreamDao,
    private val entryPointFactory: EntryPointFactory
) : CombinationCalculationService {
    override fun calculate(
        developerIds: List<DeveloperId>,
        streamIds: List<StreamId>,
        page: Int,
        pageSize: Int
    ): Page<ScoredCombination> {
        // todo validate that ids are real?

        val entryPoint = entryPointFactory.create(
            developerIds.map { it.value.toString() },
            streamIds.map { it.value.toString() }
        )

        val scoredCombinations = entryPoint.computeScoredCombinations()

        val developerLookup = developerDao.findAllById(developerIds).associateBy { it.id }
        val streamLookup = streamDao.findAllById(streamIds).associateBy { it.id }

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
                        {id -> streamLookup[StreamId(id)]?.toEntity() ?: error("Stream not found") },
                    )
                }
        )
    }
}