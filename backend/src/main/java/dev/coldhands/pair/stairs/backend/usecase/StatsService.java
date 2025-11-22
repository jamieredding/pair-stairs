package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.*;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.DeveloperMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.StreamMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

// todo make kotlin
public class StatsService {

    // todo use developerdao
    private final DeveloperRepository developerRepository;
    private final StreamRepository streamRepository;
    private final CombinationEventRepository combinationEventRepository;

    public StatsService(DeveloperRepository developerRepository, StreamRepository streamRepository, CombinationEventRepository combinationEventRepository) {
        this.developerRepository = developerRepository;
        this.streamRepository = streamRepository;
        this.combinationEventRepository = combinationEventRepository;
    }

    public DeveloperStats getDeveloperStats(long developerId) {
        final List<CombinationEventEntity> events = combinationEventRepository.findByDeveloperId(developerId);
        return getDeveloperStats(developerId, events);
    }

    public DeveloperStats getDeveloperStatsBetween(long developerId, LocalDate startDate, LocalDate endDate) {
        final List<CombinationEventEntity> events = combinationEventRepository.findByDeveloperIdBetween(developerId, startDate, endDate);
        return getDeveloperStats(developerId, events);
    }

    public StreamStats getStreamStats(long streamId) {
        final List<CombinationEventEntity> events = combinationEventRepository.findByStreamId(streamId);
        return getStreamStats(streamId, events);
    }

    public StreamStats getStreamStatsBetween(long streamId, LocalDate startDate, LocalDate endDate) {
        final List<CombinationEventEntity> events = combinationEventRepository.findByStreamIdBetween(streamId, startDate, endDate);
        return getStreamStats(streamId, events);
    }

    private DeveloperStats getDeveloperStats(long developerId, List<CombinationEventEntity> events) {
        final List<PairStreamEntity> pairStreamsWithDeveloper = events.stream()
                .map(event -> event.getCombination().getPairs().stream()
                        .filter(pairStreamEntity -> pairStreamEntity.getDevelopers().stream()
                                .anyMatch(developerEntity -> developerEntity.getId() == developerId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(STR."Should have found a PairStream that had developer with id: \{developerId}"))
                )
                .toList();

        final Stream<DeveloperEntity> developerAsSoloOrOtherDeveloperInPair = pairStreamsWithDeveloper.stream()
                .map(PairStreamEntity::getDevelopers)
                .map(developerEntities -> developerEntities.size() == 1
                        ? developerEntities.getFirst()
                        : developerEntities.stream()
                        .filter(developerEntity -> developerEntity.getId() != developerId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(STR."Should have found a list of developers that contained developer with id: \{developerId}")));

        return new DeveloperStats(getRelatedDeveloperStats(developerAsSoloOrOtherDeveloperInPair), getRelatedStreamStats(pairStreamsWithDeveloper));
    }

    private List<RelatedDeveloperStats> getRelatedDeveloperStats(Stream<DeveloperEntity> allRelevantDeveloperOccurrences) {
        final List<DeveloperInfo> allDevelopers = developerRepository.findAll().stream()
                .map(DeveloperMapper::entityToInfo)
                .toList();

        final Map<DeveloperInfo, Long> developerCounts = new HashMap<>(allRelevantDeveloperOccurrences
                .map(DeveloperMapper::entityToInfo)
                .collect(groupingBy(Function.identity(), Collectors.counting())));

        allDevelopers.forEach(developerInfo -> developerCounts.putIfAbsent(developerInfo, 0L));

        return developerCounts
                .entrySet().stream()
                .map(entry -> new RelatedDeveloperStats(entry.getKey(), entry.getValue()))
                .sorted(comparing(RelatedDeveloperStats::count))
                .toList();
    }

    private List<RelatedStreamStats> getRelatedStreamStats(List<PairStreamEntity> pairStreamsWithDeveloper) {
        final List<StreamInfo> allStreams = streamRepository.findAll().stream()
                .map(StreamMapper::entityToInfo)
                .toList();

        final Map<StreamInfo, Long> streamCounts = new HashMap<>(pairStreamsWithDeveloper.stream()
                .map(PairStreamEntity::getStream)
                .map(StreamMapper::entityToInfo)
                .collect(groupingBy(Function.identity(), Collectors.counting())));

        allStreams.forEach(streamInfo -> streamCounts.putIfAbsent(streamInfo, 0L));

        return streamCounts
                .entrySet().stream()
                .map(entry -> new RelatedStreamStats(entry.getKey(), entry.getValue()))
                .sorted(comparing(RelatedStreamStats::count))
                .toList();
    }

    private StreamStats getStreamStats(long streamId, List<CombinationEventEntity> events) {
        final List<PairStreamEntity> pairStreamsWithStream = events.stream()
                .map(event -> event.getCombination().getPairs().stream()
                        .filter(pairStreamEntity -> pairStreamEntity.getStream().getId() == streamId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(STR."Should have found a PairStream that had stream with id: \{streamId}"))
                )
                .toList();

        final Stream<DeveloperEntity> allDevelopersThatWereInTheStream = pairStreamsWithStream.stream()
                .map(PairStreamEntity::getDevelopers)
                .flatMap(Collection::stream);

        return new StreamStats(getRelatedDeveloperStats(allDevelopersThatWereInTheStream));
    }

}
