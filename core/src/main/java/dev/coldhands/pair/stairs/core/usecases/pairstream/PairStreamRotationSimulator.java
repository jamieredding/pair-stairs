package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.RotationSimulator;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import dev.coldhands.pair.stairs.core.infrastructure.InMemoryCombinationHistoryRepository;

import java.time.LocalDate;
import java.util.Collection;

public class PairStreamRotationSimulator implements RotationSimulator<PairStream> {

    private final PairStreamEntryPoint entryPoint;
    private final InMemoryCombinationHistoryRepository<PairStream> repository;
    private final PairStreamStatisticsService statisticsService;

    private LocalDate today = LocalDate.now();

    public PairStreamRotationSimulator(Collection<String> developers, Collection<String> streams, InMemoryCombinationHistoryRepository<PairStream> repository) {
        this.repository = repository;
        this.statisticsService = new PairStreamStatisticsService(repository, developers, streams, 5);
        this.entryPoint = new PairStreamEntryPoint(developers, streams, repository, statisticsService);
    }

    @Override
    public ScoredCombination<PairStream> stepSimulation() {
        final var pickFirstCombination = entryPoint.computeScoredCombinations().getFirst();

        repository.saveCombination(pickFirstCombination.combination(), today);
        statisticsService.updateStatistics();

        today = today.plusDays(1);

        return pickFirstCombination;
    }
}
