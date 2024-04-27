package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.CombinationCalculationService;
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.DeveloperMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.PairStreamMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.ScoredCombinationMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.StreamMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamEntryPoint;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class CoreCombinationCalculationService implements CombinationCalculationService {

    private final DeveloperRepository developerRepository;
    private final StreamRepository streamRepository;
    private final EntryPointFactory entryPointFactory;
    private final int pageSize;

    public CoreCombinationCalculationService(DeveloperRepository developerRepository, StreamRepository streamRepository, EntryPointFactory entryPointFactory, int pageSize) {
        this.developerRepository = developerRepository;
        this.streamRepository = streamRepository;
        this.entryPointFactory = entryPointFactory;
        this.pageSize = pageSize;
    }

    @Override
    public List<ScoredCombination> calculate(List<Long> developerIds, List<Long> streamIds, int page) {
        // todo validate that ids are real?

        final PairStreamEntryPoint entryPoint = entryPointFactory.create(
                asStrings(developerIds),
                asStrings(streamIds)
        );

        final List<dev.coldhands.pair.stairs.core.domain.ScoredCombination<PairStream>> scoredCombinations = entryPoint.computeScoredCombinations();

        final DeveloperMapper developerMapper = getDeveloperMapper(developerIds);
        final StreamMapper streamMapper = getStreamMapper(streamIds);
        final PairStreamMapper pairStreamMapper = new PairStreamMapper(developerMapper, streamMapper);
        final ScoredCombinationMapper scoredCombinationMapper = new ScoredCombinationMapper(pairStreamMapper);

        return scoredCombinations.stream()
                .skip((long) page * pageSize)
                .limit(pageSize)
                .map(scoredCombinationMapper::coreToDomain)
                .toList();
    }

    private StreamMapper getStreamMapper(List<Long> streamIds) {
        final Map<Long, StreamEntity> streamLookup = streamRepository.findAllById(streamIds).stream()
                .collect(toMap(StreamEntity::getId, s -> s));
        return new StreamMapper(streamLookup);
    }

    private DeveloperMapper getDeveloperMapper(List<Long> developerIds) {
        final Map<Long, DeveloperEntity> developerLookup = developerRepository.findAllById(developerIds).stream()
                .collect(toMap(DeveloperEntity::getId, d -> d));
        return new DeveloperMapper(developerLookup);
    }

    private static List<String> asStrings(List<Long> ids) {
        return ids.stream().map(Object::toString).toList();
    }
}
