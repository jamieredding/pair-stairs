package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.CombinationHistoryRepository;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

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
                .map(entity -> {
                    final Set<PairStream> pairs = entity.getCombination().getPairs().stream()
                            .map(pairStream -> {
                                final Set<String> developerIds = pairStream.getDevelopers().stream()
                                        .map(DeveloperEntity::getId)
                                        .map(Object::toString)
                                        .collect(toSet());

                                final String streamId = pairStream.getStream().getId().toString();
                                return new PairStream(developerIds, streamId);
                            })
                            .collect(toSet());

                    return new Combination<>(pairs);
                })
                .toList();
    }
}
