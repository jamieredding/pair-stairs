package dev.coldhands.pair.stairs.backend.infrastructure.wiring;

import dev.coldhands.pair.stairs.backend.domain.CombinationCalculationService;
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.PairStreamRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository;
import dev.coldhands.pair.stairs.backend.usecase.BackendCombinationHistoryRepository;
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService;
import dev.coldhands.pair.stairs.backend.usecase.CoreCombinationCalculationService;
import dev.coldhands.pair.stairs.backend.usecase.EntryPointFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CombinationConfig {

    private final DeveloperDao developerDao;
    private final StreamRepository streamRepository;
    private final PairStreamRepository pairStreamRepository;
    private final CombinationRepository combinationRepository;
    private final CombinationEventRepository combinationEventRepository;

    @Autowired
    public CombinationConfig(DeveloperDao developerDao, StreamRepository streamRepository, PairStreamRepository pairStreamRepository, CombinationRepository combinationRepository, CombinationEventRepository combinationEventRepository) {
        this.developerDao = developerDao;
        this.streamRepository = streamRepository;
        this.pairStreamRepository = pairStreamRepository;
        this.combinationRepository = combinationRepository;
        this.combinationEventRepository = combinationEventRepository;
    }

    @Bean
    public CombinationCalculationService combinationCalculationService() {
        return new CoreCombinationCalculationService(developerDao,
                streamRepository,
                new EntryPointFactory(backendCombinationHistoryRepository()));
    }

    @Bean
    public CombinationEventService combinationEventService() {
        return new CombinationEventService(developerDao, streamRepository, pairStreamRepository, combinationRepository, combinationEventRepository);
    }

    @Bean
    public BackendCombinationHistoryRepository backendCombinationHistoryRepository() {
        return new BackendCombinationHistoryRepository(combinationEventRepository);
    }
}
