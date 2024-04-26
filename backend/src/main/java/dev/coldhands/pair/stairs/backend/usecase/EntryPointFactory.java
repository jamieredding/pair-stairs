package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamEntryPoint;
import dev.coldhands.pair.stairs.core.usecases.pairstream.PairStreamStatisticsService;

import java.util.List;

public class EntryPointFactory {

    private static final int NUMBER_OF_PREVIOUS_COMBINATIONS_TO_CONSIDER = 5; // todo this should be configurable

    private final CombinationHistoryRepository<PairStream> repository;

    public EntryPointFactory(CombinationHistoryRepository<PairStream> repository) {
        this.repository = repository;
    }

    public PairStreamEntryPoint create(List<String> developers, List<String> streams) {
        final var statisticsService = new PairStreamStatisticsService(repository, developers, streams, NUMBER_OF_PREVIOUS_COMBINATIONS_TO_CONSIDER);

        return new PairStreamEntryPoint(developers, streams, repository, statisticsService);
    }
}
