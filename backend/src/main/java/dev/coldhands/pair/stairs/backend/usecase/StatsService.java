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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

public class StatsService {

    private final DeveloperRepository developerRepository;
    private final StreamRepository streamRepository;
    private final CombinationEventRepository combinationEventRepository;

    public StatsService(DeveloperRepository developerRepository, StreamRepository streamRepository, CombinationEventRepository combinationEventRepository) {
        this.developerRepository = developerRepository;
        this.streamRepository = streamRepository;
        this.combinationEventRepository = combinationEventRepository;
    }

    public DeveloperStats getPairOccurrencesForDeveloper(long id) {
        final List<CombinationEventEntity> events = combinationEventRepository.findByDeveloperId(id);
        final List<PairStreamEntity> pairStreamsWithDeveloper = events.stream()
                .map(event -> event.getCombination().getPairs().stream()
                        .filter(pairStreamEntity -> pairStreamEntity.getDevelopers().stream()
                                .anyMatch(developerEntity -> developerEntity.getId() == id))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(STR."Should have found a PairStream that had developer with id: \{id}"))
                )
                .toList();

        return new DeveloperStats(getRelatedDeveloperStats(id, pairStreamsWithDeveloper), getRelatedStreamStats(pairStreamsWithDeveloper));
    }

    private List<RelatedDeveloperStats> getRelatedDeveloperStats(long developerId, List<PairStreamEntity> pairStreamsWithDeveloper) {
        final List<DeveloperInfo> allDevelopers = developerRepository.findAll().stream()
                .map(DeveloperMapper::entityToInfo)
                .toList();

        final Stream<DeveloperEntity> developerAsSoloOrOtherDeveloperInPair = pairStreamsWithDeveloper.stream()
                .map(PairStreamEntity::getDevelopers)
                .map(developerEntities -> developerEntities.size() == 1
                        ? developerEntities.getFirst()
                        : developerEntities.stream()
                        .filter(developerEntity -> developerEntity.getId() != developerId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(STR."Should have found a list of developers that contained developer with id: \{developerId}")));
        
        final Map<DeveloperInfo, Long> developerOccurrences = new HashMap<>(developerAsSoloOrOtherDeveloperInPair
                .map(DeveloperMapper::entityToInfo)
                .collect(groupingBy(Function.identity(), Collectors.counting())));

        allDevelopers.forEach(developerInfo -> developerOccurrences.putIfAbsent(developerInfo, 0L));

        return developerOccurrences
                .entrySet().stream()
                .map(entry -> new RelatedDeveloperStats(entry.getKey(), entry.getValue()))
                .sorted(comparing(RelatedDeveloperStats::count))
                .toList();
    }

    private List<RelatedStreamStats> getRelatedStreamStats(List<PairStreamEntity> pairStreamsWithDeveloper) {
        final List<StreamInfo> allStreams = streamRepository.findAll().stream()
                .map(StreamMapper::entityToInfo)
                .toList();

        final Map<StreamInfo, Long> streamOccurrences = new HashMap<>(pairStreamsWithDeveloper.stream()
                .map(PairStreamEntity::getStream)
                .map(StreamMapper::entityToInfo)
                .collect(groupingBy(Function.identity(), Collectors.counting())));

        allStreams.forEach(streamInfo -> streamOccurrences.putIfAbsent(streamInfo, 0L));

        return streamOccurrences
                .entrySet().stream()
                .map(entry -> new RelatedStreamStats(entry.getKey(), entry.getValue()))
                .sorted(comparing(RelatedStreamStats::count))
                .toList();
    }
}
