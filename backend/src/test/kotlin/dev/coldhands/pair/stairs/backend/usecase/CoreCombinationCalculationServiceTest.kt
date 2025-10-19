package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo
import dev.coldhands.pair.stairs.backend.domain.PairStream
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination
import dev.coldhands.pair.stairs.backend.domain.StreamInfo
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository
import dev.coldhands.pair.stairs.core.domain.Combination
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamEntryPoint
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import dev.coldhands.pair.stairs.core.domain.ScoredCombination as CoreScoredCombination
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream as CorePairStream

class CoreCombinationCalculationServiceTest {
    private val entryPointFactory: EntryPointFactory = mockk()
    private val entryPoint: PairStreamEntryPoint = mockk()
    private val developerRepository: DeveloperRepository = mockk()
    private val streamRepository: StreamRepository = mockk()
    private var underTest: CoreCombinationCalculationService =
        CoreCombinationCalculationService(developerRepository, streamRepository, entryPointFactory)

    @Test
    fun runCoreEntryPointUsingInput() {
        every { developerRepository.findAllById(listOf(0L, 1L, 2L)) } returns listOf(
            DeveloperEntity(0L, "dev-0", false),
            DeveloperEntity(1L, "dev-1", false),
            DeveloperEntity(2L, "dev-2", false)
        )
        every { streamRepository.findAllById(listOf(0L, 1L)) } returns listOf(
            StreamEntity(0L, "stream-a", false),
            StreamEntity(1L, "stream-b", false)
        )
        every {
            entryPointFactory.create(
                listOf("0", "1", "2"),
                listOf("0", "1")
            )
        } returns entryPoint

        every { entryPoint.computeScoredCombinations() } returns listOf(
            CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf("0", "1"), "0"),
                        CorePairStream(setOf("2"), "1")
                    )
                ),
                10,
                listOf()
            ),
            CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf("0", "2"), "0"),
                        CorePairStream(setOf("1"), "1")
                    )
                ),
                20,
                listOf()
            )
        )


        underTest.calculate(listOf(0L, 1L, 2L), listOf(0L, 1L), 0, 10) shouldContainExactly listOf(
            ScoredCombination(
                10,
                listOf(
                    PairStream(
                        listOf(
                            DeveloperInfo(0, "dev-0", false),
                            DeveloperInfo(1, "dev-1", false)
                        ),
                        StreamInfo(0, "stream-a", false)
                    ),
                    PairStream(
                        listOf(
                            DeveloperInfo(2, "dev-2", false)
                        ),
                        StreamInfo(1, "stream-b", false)
                    )
                )
            ),
            ScoredCombination(
                20,
                listOf(
                    PairStream(
                        listOf(
                            DeveloperInfo(0, "dev-0", false),
                            DeveloperInfo(2, "dev-2", false)
                        ),
                        StreamInfo(0, "stream-a", false)
                    ),
                    PairStream(
                        listOf(
                            DeveloperInfo(1, "dev-1", false)
                        ),
                        StreamInfo(1, "stream-b", false)
                    )
                )
            )
        )
    }

    @Nested
    internal inner class Pagination {
        @Test
        fun onlyReturnFirstPageOfScoredCombinationsIfMoreThanPageSizeAreReturned() {
            every { developerRepository.findAllById(listOf(0L)) } returns listOf(
                DeveloperEntity(0L, "dev-0", false),
            )
            every { streamRepository.findAllById(listOf(0L)) } returns listOf(
                StreamEntity(0L, "stream-a", false),
            )
            every {
                entryPointFactory.create(
                    listOf("0"),
                    listOf("0")
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10),
                coreScoredCombinationWithScore(20),
                coreScoredCombinationWithScore(30)
            )

            val pageSize = 2
            val requestedPage = 0
            val underTest = CoreCombinationCalculationService(developerRepository, streamRepository, entryPointFactory)

            val scoredCombinations =
                underTest.calculate(listOf(0L), listOf(0L), requestedPage, pageSize)

            scoredCombinations.map { it.score } shouldContainExactly listOf(10, 20)
        }

        @Test
        fun returnSecondPageOfScoredCombinationsWhenRequested() {
            every { developerRepository.findAllById(listOf(0L)) } returns listOf(
                DeveloperEntity(0L, "dev-0", false),
            )
            every { streamRepository.findAllById(listOf(0L)) } returns listOf(
                StreamEntity(0L, "stream-a", false),
            )
            every {
                entryPointFactory.create(
                    listOf("0"),
                    listOf("0")
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10),
                coreScoredCombinationWithScore(20),
                coreScoredCombinationWithScore(30)
            )

            val pageSize = 2
            val requestedPage = 1
            val underTest = CoreCombinationCalculationService(developerRepository, streamRepository, entryPointFactory)

            val scoredCombinations =
                underTest.calculate(listOf(0L), listOf(0L), requestedPage, pageSize)

            scoredCombinations.map { it.score } shouldContainExactly listOf(30)
        }

    }

    companion object {
        private fun coreScoredCombinationWithScore(totalScore: Int): CoreScoredCombination<CorePairStream> {
            return CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf("0"), "0")
                    )
                ),
                totalScore,
                listOf()
            )
        }
    }

}