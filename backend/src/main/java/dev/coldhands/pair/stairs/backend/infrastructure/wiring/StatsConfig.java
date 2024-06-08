package dev.coldhands.pair.stairs.backend.infrastructure.wiring;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.CombinationEventRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository;
import dev.coldhands.pair.stairs.backend.usecase.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsConfig {

    private final DeveloperRepository developerRepository;
    private final StreamRepository streamRepository;
    private final CombinationEventRepository combinationEventRepository;

    @Autowired
    public StatsConfig(DeveloperRepository developerRepository, StreamRepository streamRepository, CombinationEventRepository combinationEventRepository) {
        this.developerRepository = developerRepository;
        this.streamRepository = streamRepository;
        this.combinationEventRepository = combinationEventRepository;
    }

    @Bean
    public StatsService statsService() {
        return new StatsService(developerRepository, streamRepository, combinationEventRepository);
    }
}
