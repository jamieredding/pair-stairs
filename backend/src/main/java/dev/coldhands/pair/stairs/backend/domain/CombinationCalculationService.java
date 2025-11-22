package dev.coldhands.pair.stairs.backend.domain;

import java.util.List;

public interface CombinationCalculationService {

    Page<ScoredCombination> calculate(List<DeveloperId> developerIds, List<Long> streamIds, int page, int pageSize);
}
