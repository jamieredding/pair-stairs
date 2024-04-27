package dev.coldhands.pair.stairs.backend.infrastructure.wiring;

import dev.coldhands.pair.stairs.backend.domain.CombinationCalculationService;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.*;
import dev.coldhands.pair.stairs.backend.usecase.BackendCombinationHistoryRepository;
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService;
import dev.coldhands.pair.stairs.backend.usecase.CoreCombinationCalculationService;
import dev.coldhands.pair.stairs.backend.usecase.EntryPointFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CombinationConfig {

    private final DeveloperRepository developerRepository;
    private final StreamRepository streamRepository;
    private final PairStreamRepository pairStreamRepository;
    private final CombinationRepository combinationRepository;
    private final CombinationEventRepository combinationEventRepository;

    @Autowired
    public CombinationConfig(DeveloperRepository developerRepository, StreamRepository streamRepository, PairStreamRepository pairStreamRepository, CombinationRepository combinationRepository, CombinationEventRepository combinationEventRepository) {
        this.developerRepository = developerRepository;
        this.streamRepository = streamRepository;
        this.pairStreamRepository = pairStreamRepository;
        this.combinationRepository = combinationRepository;
        this.combinationEventRepository = combinationEventRepository;
    }

    @Bean
    public CombinationCalculationService combinationCalculationService() {
        return new CoreCombinationCalculationService(developerRepository,
                streamRepository,
                new EntryPointFactory(backendCombinationHistoryRepository()),
                3); // todo stop hardcoding 3
    }

    @Bean
    public CombinationEventService combinationEventService() {
        return new CombinationEventService(developerRepository, streamRepository, pairStreamRepository, combinationRepository, combinationEventRepository);
    }

    @Bean
    public BackendCombinationHistoryRepository backendCombinationHistoryRepository() {
        return new BackendCombinationHistoryRepository(combinationEventRepository);
    }
}
