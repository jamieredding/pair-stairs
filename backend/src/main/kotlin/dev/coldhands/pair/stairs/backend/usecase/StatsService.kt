package dev.coldhands.pair.stairs.backend.usecase

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEventDao
import dev.coldhands.pair.stairs.backend.domain.combination.PairStream
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperInfo
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperStats
import dev.coldhands.pair.stairs.backend.domain.developer.RelatedDeveloperStats
import dev.coldhands.pair.stairs.backend.domain.stream.RelatedStreamStats
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamInfo
import dev.coldhands.pair.stairs.backend.domain.stream.StreamStats
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toInfo
import java.time.LocalDate

class StatsService(
    private val developerDao: DeveloperDao,
    private val streamDao: StreamDao,
    private val combinationEventDao: CombinationEventDao,
) {

    fun getDeveloperStats(developerId: DeveloperId): DeveloperStats {
        val events = combinationEventDao.findByDeveloperId(developerId)
        return getDeveloperStats(developerId, events)
    }

    fun getDeveloperStatsBetween(
        developerId: DeveloperId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): DeveloperStats {
        val events = combinationEventDao.findByDeveloperIdBetween(developerId, startDate, endDate)
        return getDeveloperStats(developerId, events)
    }

    fun getStreamStats(streamId: StreamId): StreamStats {
        val events = combinationEventDao.findByStreamId(streamId)
        return getStreamStats(streamId, events)
    }

    fun getStreamStatsBetween(
        streamId: StreamId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): StreamStats {
        val events = combinationEventDao.findByStreamIdBetween(streamId, startDate, endDate)
        return getStreamStats(streamId, events)
    }

    private fun getDeveloperStats(
        developerId: DeveloperId,
        events: Set<CombinationEvent>,
    ): DeveloperStats {
        val pairStreamsWithDeveloper: List<PairStream> = events.map { event ->
            event.combination
                .firstOrNull { pairStream ->
                    pairStream.developerIds.any { it == developerId }
                }
                ?: error("Should have found a PairStream that had developer with id: $developerId")
        }

        val developerAsSoloOrOtherDeveloperInPair: List<DeveloperId> =
            pairStreamsWithDeveloper.map { pairStream ->
                val developers = pairStream.developerIds
                if (developers.size == 1) {
                    developers.first()
                } else {
                    developers.firstOrNull { it != developerId }
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
        allRelevantDeveloperOccurrences: Collection<DeveloperId>,
    ): List<RelatedDeveloperStats> {
        val allDevelopers: List<DeveloperInfo> = developerDao.findAll()
            .map { it.toInfo() }

        val developerCounts: MutableMap<DeveloperInfo, Long> = allRelevantDeveloperOccurrences
            .map { developerId -> allDevelopers.first { it.id == developerId } }
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
        pairStreamsWithDeveloper: List<PairStream>,
    ): List<RelatedStreamStats> {
        val allStreams: List<StreamInfo> = streamDao.findAll()
            .map { it.toInfo() }

        val streamCounts: MutableMap<StreamInfo, Long> = pairStreamsWithDeveloper
            .map { it.streamId }
            .map { streamId -> allStreams.first { it.id == streamId } }
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
        streamId: StreamId,
        events: Set<CombinationEvent>,
    ): StreamStats {
        val pairStreamsWithStream: List<PairStream> = events.map { event ->
            event.combination
                .firstOrNull { pairStream ->
                    pairStream.streamId == streamId
                }
                ?: error("Should have found a PairStream that had stream with id: $streamId")
        }

        val allDevelopersThatWereInTheStream: List<DeveloperId> = pairStreamsWithStream
            .flatMap { it.developerIds }

        return StreamStats(
            developerStats = getRelatedDeveloperStats(allDevelopersThatWereInTheStream),
        )
    }
}