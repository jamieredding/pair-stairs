package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

public class BackendCombinationHistoryRepository implements CombinationHistoryRepository<PairStream> {

    private final CombinationEventRepository repository;

    public BackendCombinationHistoryRepository(CombinationEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Combination<PairStream>> getMostRecentCombinations(int count) {
        final PageRequest pageRequest = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "date"));

        final List<CombinationEventEntity> entities = repository.findAll(pageRequest).toList();

        return entities.stream()
                .map(CombinationEventEntity::getCombination)
                .map(CombinationMapper::entityToCore)
                .toList();
    }
}
