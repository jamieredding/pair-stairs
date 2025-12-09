package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.Page
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationCalculationService
import dev.coldhands.pair.stairs.backend.domain.combination.PairStream
import dev.coldhands.pair.stairs.backend.domain.combination.ScoredCombination

class CoreCombinationCalculationService(
    private val entryPointFactory: EntryPointFactory
) : CombinationCalculationService {
    override fun calculate(
        developerIds: Collection<DeveloperId>,
        streamIds: Collection<StreamId>,
        page: Int,
        pageSize: Int
    ): Page<ScoredCombination> {
        // todo validate that ids are real?

        val entryPoint = entryPointFactory.create(
            developerIds.map { it.value.toString() },
            streamIds.map { it.value.toString() }
        )

        val scoredCombinations = entryPoint.computeScoredCombinations()

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
                    ScoredCombination(
                        score = sc.totalScore,
                        combination = sc.combination.pairs.map { pairStream ->
                            PairStream(
                                developerIds = pairStream.developers().map { DeveloperId(it.toLong()) }.toSet(),
                                streamId = StreamId(pairStream.stream.toLong())
                            )
                        }.toSet()
                    )
                }
        )
    }
}