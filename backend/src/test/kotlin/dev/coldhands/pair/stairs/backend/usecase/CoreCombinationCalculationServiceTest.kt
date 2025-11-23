package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.aStreamDetails
import dev.coldhands.pair.stairs.backend.asString
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.PairStream
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeDeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao.FakeStreamDao
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
    private val streamDao: StreamDao = FakeStreamDao()
    private var underTest: CoreCombinationCalculationService =
        CoreCombinationCalculationService(developerDao, streamDao, entryPointFactory)

    @Test
    fun runCoreEntryPointUsingInput() {
        val developers = listOf(
            aDeveloperDetails("dev-0"),
            aDeveloperDetails("dev-1"),
            aDeveloperDetails("dev-2")
        ).map { developerDao.create(it).shouldBeSuccess() }
        val developerIds = developers.map { it.id }
        val streams = listOf(
            aStreamDetails("stream-a"),
            aStreamDetails("stream-b"),
        ).map { streamDao.create(it).shouldBeSuccess() }
        val streamIds = streams.map { it.id }

        every {
            entryPointFactory.create(
                developerIds.map { it.asString() },
                streamIds.map { it.asString() }
            )
        } returns entryPoint

        every { entryPoint.computeScoredCombinations() } returns listOf(
            CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf(developerIds[0].asString(), developerIds[1].asString()), streamIds[0].asString()),
                        CorePairStream(setOf(developerIds[2].asString()), streamIds[1].asString())
                    )
                ),
                10,
                listOf()
            ),
            CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf(developerIds[0].asString(), developerIds[2].asString()), streamIds[0].asString()),
                        CorePairStream(setOf(developerIds[1].asString()), streamIds[1].asString())
                    )
                ),
                20,
                listOf()
            )
        )


        underTest.calculate(developerIds, streamIds, 0, 10).data shouldContainExactly listOf(
            ScoredCombination(
                10,
                listOf(
                    PairStream(
                        listOf(
                            developers[0].toInfo(),
                            developers[1].toInfo(),
                        ),
                        streams[0].toInfo()
                    ),
                    PairStream(
                        listOf(
                            developers[2].toInfo(),
                        ),
                        streams[1].toInfo()
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
                        streams[0].toInfo()
                    ),
                    PairStream(
                        listOf(
                            developers[1].toInfo(),
                        ),
                        streams[1].toInfo()
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

            val streams = listOf(
                aStreamDetails("stream-a")
            ).map { streamDao.create(it).shouldBeSuccess() }
            val streamIds = streams.map { it.id }

            every {
                entryPointFactory.create(
                    developerIds.map { it.asString() },
                    streamIds.map { it.asString() }
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10, developerIds[0], streamIds[0]),
                coreScoredCombinationWithScore(20, developerIds[0], streamIds[0]),
                coreScoredCombinationWithScore(30, developerIds[0], streamIds[0])
            )

            val pageSize = 2
            val requestedPage = 0
            val underTest = CoreCombinationCalculationService(developerDao, streamDao, entryPointFactory)

            underTest.calculate(developerIds, streamIds, requestedPage, pageSize) should { page ->
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

            val streams = listOf(
                aStreamDetails("stream-a")
            ).map { streamDao.create(it).shouldBeSuccess() }
            val streamIds = streams.map { it.id }
            every {
                entryPointFactory.create(
                    developerIds.map { it.asString() },
                    streamIds.map { it.asString() }
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10, developerIds[0], streamIds[0]),
                coreScoredCombinationWithScore(20, developerIds[0], streamIds[0]),
                coreScoredCombinationWithScore(30, developerIds[0], streamIds[0])
            )

            val pageSize = 2
            val requestedPage = 1
            val underTest = CoreCombinationCalculationService(developerDao, streamDao, entryPointFactory)

            underTest.calculate(developerIds, streamIds, requestedPage, pageSize) should { page ->
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
            val streams = listOf(
                aStreamDetails("stream-a")
            ).map { streamDao.create(it).shouldBeSuccess() }
            val streamIds = streams.map { it.id }
            every {
                entryPointFactory.create(
                    developerIds.map { it.asString() },
                    streamIds.map { it.asString() }
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10, developerIds[0], streamIds[0]),
                coreScoredCombinationWithScore(20, developerIds[0], streamIds[0]),
            )

            val pageSize = 1
            val requestedPage = -1
            val underTest = CoreCombinationCalculationService(developerDao, streamDao, entryPointFactory)

            underTest.calculate(developerIds, streamIds, requestedPage, pageSize) should { page ->
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
            val streams = listOf(
                aStreamDetails("stream-a")
            ).map { streamDao.create(it).shouldBeSuccess() }
            val streamIds = streams.map { it.id }
            every {
                entryPointFactory.create(
                    developerIds.map { it.asString() },
                    streamIds.map { it.asString() }
                )
            } returns entryPoint

            every { entryPoint.computeScoredCombinations() } returns listOf(
                coreScoredCombinationWithScore(10, developerIds[0], streamIds[0]),
                coreScoredCombinationWithScore(20, developerIds[0], streamIds[0]),
            )

            val pageSize = 1
            val requestedPage = 2
            val underTest = CoreCombinationCalculationService(developerDao, streamDao, entryPointFactory)

            underTest.calculate(developerIds, streamIds, requestedPage, pageSize) should { page ->
                page.metadata should { metadata ->
                    metadata.nextPageNumber.shouldBeNull()
                }
                page.data.shouldBeEmpty()
            }
        }
    }

    companion object {
        private fun coreScoredCombinationWithScore(totalScore: Int, developerId: DeveloperId, streamId: StreamId): CoreScoredCombination<CorePairStream> {
            return CoreScoredCombination(
                Combination(
                    setOf(
                        CorePairStream(setOf(developerId.asString()), streamId.asString())
                    )
                ),
                totalScore,
                listOf()
            )
        }
    }

}