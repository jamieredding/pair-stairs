package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.asString
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.PairStream
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination
import dev.coldhands.pair.stairs.backend.domain.StreamInfo
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeDeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository
import dev.coldhands.pair.stairs.core.domain.Combination
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamEntryPoint
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import dev.coldhands.pair.stairs.core.domain.ScoredCombination as CoreScoredCombination
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream as CorePairStream

class CoreCombinationCalculationServiceTest {
    private val entryPointFactory: EntryPointFactory = mockk()
    private val entryPoint: PairStreamEntryPoint = mockk()
    private val developerDao: DeveloperDao = FakeDeveloperDao()
    private val streamRepository: StreamRepository = mockk()
    private var underTest: CoreCombinationCalculationService =
        CoreCombinationCalculationService(developerDao, streamRepository, entryPointFactory)

    @Test
    fun runCoreEntryPointUsingInput() {
        val developers = listOf(
            aDeveloperDetails("dev-0"),
            aDeveloperDetails("dev-1"),
            aDeveloperDetails("dev-2")
        ).map { developerDao.create(it).shouldBeSuccess() }
        val developerIds = developers.map { it.id }

        every { streamRepository.findAllById(listOf(0L, 1L)) } returns listOf(
            StreamEntity(0L, "stream-a", false),
            StreamEntity(1L, "stream-b", false)
        )
        every {
            entryPointFactory.create(
                developerIds.map { it.asString() },
                listOf("0", "1")
            )
        } returns entryPoint

        every { entryPoint.computeScoredCombinations() } returns listOf(
            CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf(developerIds[0].asString(), developerIds[1].asString()), "0"),
                        CorePairStream(setOf(developerIds[2].asString()), "1")
                    )
                ),
                10,
                listOf()
            ),
            CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf(developerIds[0].asString(), developerIds[2].asString()), "0"),
                        CorePairStream(setOf(developerIds[1].asString()), "1")
                    )
                ),
                20,
                listOf()
            )
        )


        underTest.calculate(developerIds, listOf(0L, 1L), 0, 10).data shouldContainExactly listOf(
            ScoredCombination(
                10,
                listOf(
                    PairStream(
                        listOf(
                            developers[0].toInfo(),
                            developers[1].toInfo(),
                        ),
                        StreamInfo(0, "stream-a", false)
                    ),
                    PairStream(
                        listOf(
                            developers[2].toInfo(),
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
                            developers[0].toInfo(),
                            developers[2].toInfo(),
                        ),
                        StreamInfo(0, "stream-a", false)
                    ),
                    PairStream(
                        listOf(
                            developers[1].toInfo(),
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
            val developers = listOf(
                aDeveloperDetails("dev-0")
            ).map { developerDao.create(it).shouldBeSuccess() }
            val developerIds = developers.map { it.id }

            every { streamRepository.findAllById(listOf(0L)) } returns listOf(
                StreamEntity(0L, "stream-a", false),
            )
            every {
                entryPointFactory.create(
                    developerIds.map { it.asString() },
                    listOf("0")
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10, developerIds[0]),
                coreScoredCombinationWithScore(20, developerIds[0]),
                coreScoredCombinationWithScore(30, developerIds[0])
            )

            val pageSize = 2
            val requestedPage = 0
            val underTest = CoreCombinationCalculationService(developerDao, streamRepository, entryPointFactory)

            underTest.calculate(developerIds, listOf(0L), requestedPage, pageSize) should { page ->
                page.metadata should { metadata ->
                    metadata.nextPageNumber shouldBe 1
                }
                page.data should { scoredCombinations ->
                    scoredCombinations.map { it.score } shouldContainExactly listOf(10, 20)
                }
            }
        }

        @Test
        fun returnSecondPageOfScoredCombinationsWhenRequested() {
            val developers = listOf(
                aDeveloperDetails("dev-0")
            ).map { developerDao.create(it).shouldBeSuccess() }
            val developerIds = developers.map { it.id }

            every { streamRepository.findAllById(listOf(0L)) } returns listOf(
                StreamEntity(0L, "stream-a", false),
            )
            every {
                entryPointFactory.create(
                    developerIds.map { it.asString() },
                    listOf("0")
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10, developerIds[0]),
                coreScoredCombinationWithScore(20, developerIds[0]),
                coreScoredCombinationWithScore(30, developerIds[0])
            )

            val pageSize = 2
            val requestedPage = 1
            val underTest = CoreCombinationCalculationService(developerDao, streamRepository, entryPointFactory)

            underTest.calculate(developerIds, listOf(0L), requestedPage, pageSize) should { page ->
                page.metadata should { metadata ->
                    metadata.nextPageNumber.shouldBeNull()
                }
                page.data should { scoredCombinations ->
                    scoredCombinations.map { it.score } shouldContainExactly listOf(30)
                }
            }
        }

        @Test
        fun returnEmptyPageIfTooEarlyOfAPageRequested() {
            val developers = listOf(
                aDeveloperDetails("dev-0")
            ).map { developerDao.create(it).shouldBeSuccess() }
            val developerIds = developers.map { it.id }
            every { streamRepository.findAllById(listOf(0L)) } returns listOf(
                StreamEntity(0L, "stream-a", false),
            )
            every {
                entryPointFactory.create(
                    developerIds.map { it.asString() },
                    listOf("0")
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10, developerIds[0]),
                coreScoredCombinationWithScore(20, developerIds[0]),
            )

            val pageSize = 1
            val requestedPage = -1
            val underTest = CoreCombinationCalculationService(developerDao, streamRepository, entryPointFactory)

            underTest.calculate(developerIds, listOf(0L), requestedPage, pageSize) should { page ->
                page.metadata should { metadata ->
                    metadata.nextPageNumber.shouldBeNull()
                }
                page.data.shouldBeEmpty()
            }
        }

        @Test
        fun returnEmptyPageIfTooLateOfAPageRequested() {
            val developers = listOf(
                aDeveloperDetails("dev-0")
            ).map { developerDao.create(it).shouldBeSuccess() }
            val developerIds = developers.map { it.id }
            every { streamRepository.findAllById(listOf(0L)) } returns listOf(
                StreamEntity(0L, "stream-a", false),
            )
            every {
                entryPointFactory.create(
                    developerIds.map { it.asString() },
                    listOf("0")
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10, developerIds[0]),
                coreScoredCombinationWithScore(20, developerIds[0]),
            )

            val pageSize = 1
            val requestedPage = 2
            val underTest = CoreCombinationCalculationService(developerDao, streamRepository, entryPointFactory)

            underTest.calculate(developerIds, listOf(0L), requestedPage, pageSize) should { page ->
                page.metadata should { metadata ->
                    metadata.nextPageNumber.shouldBeNull()
                }
                page.data.shouldBeEmpty()
            }
        }
    }

    companion object {
        private fun coreScoredCombinationWithScore(totalScore: Int, developerId: DeveloperId): CoreScoredCombination<CorePairStream> {
            return CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf(developerId.asString()), "0")
                    )
                ),
                totalScore,
                listOf()
            )
        }
    }

}