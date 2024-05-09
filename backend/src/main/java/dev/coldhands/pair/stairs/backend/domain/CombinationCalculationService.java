package dev.coldhands.pair.stairs.backend.domain;

import java.util.List;

public interface CombinationCalculationService {

    List<ScoredCombination> calculate(List<Long> developerIds, List<Long> streamIds, int page, int pageSize);
}
