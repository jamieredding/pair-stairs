package dev.coldhands.pair.stairs.core.usecases.pairstream;

import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.RotationSimulator;
import dev.coldhands.pair.stairs.core.domain.ScoredCombination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStreamCombination;

import java.time.LocalDate;
import java.util.Collection;

public class PairStreamRotationSimulator implements RotationSimulator<PairStreamCombination> {

    private final PairStreamEntryPoint entryPoint;
    private final CombinationHistoryRepository<PairStreamCombination> repository;

    private LocalDate today = LocalDate.now();

    public PairStreamRotationSimulator(Collection<String> developers, Collection<String> streams, CombinationHistoryRepository<PairStreamCombination> repository) {
        this.repository = repository;
        this.entryPoint = new PairStreamEntryPoint(developers, streams, repository);
    }

    @Override
    public ScoredCombination<PairStreamCombination> stepSimulation() {
        final var pickFirstCombination = entryPoint.computeScoredCombinations().getFirst();

        repository.saveCombination(pickFirstCombination.combination(), today);

        today = today.plusDays(1);

        return pickFirstCombination;
    }
}
