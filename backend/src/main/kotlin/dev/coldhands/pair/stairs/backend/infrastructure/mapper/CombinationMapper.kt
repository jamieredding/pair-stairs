package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.domain.combination.PairStream
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.PairStreamInfo

class CombinationMapper(
    private val developerDao: DeveloperDao,
    private val streamDao: StreamDao
) {
    fun toInfo(combination: Collection<PairStream>): List<PairStreamInfo> {
        val developers = developerDao.findAll()
        val streams = streamDao.findAll()

        return combination.map { pairStream ->
            PairStreamInfo(
                developers = pairStream.developerIds.map { developerId ->
                    developers.firstOrNull { it.id == developerId }?.toInfo()
                        ?: error("bad times")
                }.sorted(),
                stream = streams.firstOrNull { it.id == pairStream.streamId }?.toInfo()
                    ?: error("bad times")
            )
        }
            .sorted()
    }
}