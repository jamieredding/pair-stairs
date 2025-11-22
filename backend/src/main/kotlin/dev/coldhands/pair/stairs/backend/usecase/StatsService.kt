package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo
import dev.coldhands.pair.stairs.backend.domain.StreamInfo
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperStats
import dev.coldhands.pair.stairs.backend.domain.developer.RelatedDeveloperStats
import dev.coldhands.pair.stairs.backend.domain.stream.RelatedStreamStats
import dev.coldhands.pair.stairs.backend.domain.stream.StreamStats
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.DeveloperMapper
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.StreamMapper
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository
import java.time.LocalDate

class StatsService(
    private val developerDao: DeveloperDao,
    private val streamRepository: StreamRepository,
    private val combinationEventRepository: CombinationEventRepository,
) {

    fun getDeveloperStats(developerId: Long): DeveloperStats {
        val events = combinationEventRepository.findByDeveloperId(developerId)
        return getDeveloperStats(developerId, events)
    }

    fun getDeveloperStatsBetween(
        developerId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): DeveloperStats {
        val events = combinationEventRepository.findByDeveloperIdBetween(developerId, startDate, endDate)
        return getDeveloperStats(developerId, events)
    }

    fun getStreamStats(streamId: Long): StreamStats {
        val events = combinationEventRepository.findByStreamId(streamId)
        return getStreamStats(streamId, events)
    }

    fun getStreamStatsBetween(
        streamId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): StreamStats {
        val events = combinationEventRepository.findByStreamIdBetween(streamId, startDate, endDate)
        return getStreamStats(streamId, events)
    }

    private fun getDeveloperStats(
        developerId: Long,
        events: List<CombinationEventEntity>,
    ): DeveloperStats {
        val pairStreamsWithDeveloper: List<PairStreamEntity> = events.map { event ->
            event.combination.pairs
                .firstOrNull { pairStreamEntity ->
                    pairStreamEntity.developers.any { developerEntity ->
                        developerEntity.id == developerId
                    }
                }
                ?: error("Should have found a PairStream that had developer with id: $developerId")
        }

        val developerAsSoloOrOtherDeveloperInPair: List<DeveloperEntity> =
            pairStreamsWithDeveloper.map { pairStreamEntity ->
                val developers = pairStreamEntity.developers
                if (developers.size == 1) {
                    developers.first()
                } else {
                    developers.firstOrNull { it.id != developerId }
                        ?: error(
                            "Should have found a list of developers that contained developer with id: $developerId",
                        )
                }
            }

        return DeveloperStats(
            developerStats = getRelatedDeveloperStats(developerAsSoloOrOtherDeveloperInPair),
            streamStats = getRelatedStreamStats(pairStreamsWithDeveloper),
        )
    }

    private fun getRelatedDeveloperStats(
        allRelevantDeveloperOccurrences: Collection<DeveloperEntity>,
    ): List<RelatedDeveloperStats> {
        val allDevelopers: List<DeveloperInfo> = developerDao.findAll()
            .map { it.toInfo() }

        val developerCounts: MutableMap<DeveloperInfo, Long> = allRelevantDeveloperOccurrences
            .map(DeveloperMapper::entityToInfo)
            .groupingBy { it }
            .eachCount()
            .mapValuesTo(mutableMapOf()) { (_, count) -> count.toLong() }

        allDevelopers.forEach { developerInfo ->
            developerCounts.putIfAbsent(developerInfo, 0L)
        }

        return developerCounts
            .map { (developerInfo, count) -> RelatedDeveloperStats(developerInfo, count) }
            .sortedBy(RelatedDeveloperStats::count)
    }

    private fun getRelatedStreamStats(
        pairStreamsWithDeveloper: List<PairStreamEntity>,
    ): List<RelatedStreamStats> {
        val allStreams: List<StreamInfo> = streamRepository.findAll()
            .map(StreamMapper::entityToInfo)

        val streamCounts: MutableMap<StreamInfo, Long> = pairStreamsWithDeveloper
            .map { it.stream }
            .map(StreamMapper::entityToInfo)
            .groupingBy { it }
            .eachCount()
            .mapValuesTo(mutableMapOf()) { (_, count) -> count.toLong() }

        allStreams.forEach { streamInfo ->
            streamCounts.putIfAbsent(streamInfo, 0L)
        }

        return streamCounts
            .map { (streamInfo, count) -> RelatedStreamStats(streamInfo, count) }
            .sortedBy(RelatedStreamStats::count)
    }

    private fun getStreamStats(
        streamId: Long,
        events: List<CombinationEventEntity>,
    ): StreamStats {
        val pairStreamsWithStream: List<PairStreamEntity> = events.map { event ->
            event.combination.pairs
                .firstOrNull { pairStreamEntity ->
                    pairStreamEntity.stream.id == streamId
                }
                ?: error("Should have found a PairStream that had stream with id: $streamId")
        }

        val allDevelopersThatWereInTheStream: List<DeveloperEntity> = pairStreamsWithStream
            .flatMap { it.developers }

        return StreamStats(
            developerStats = getRelatedDeveloperStats(allDevelopersThatWereInTheStream),
        )
    }
}