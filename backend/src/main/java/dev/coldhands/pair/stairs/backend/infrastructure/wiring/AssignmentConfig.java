package dev.coldhands.pair.stairs.backend.infrastructure.wiring;

import dev.coldhands.pair.stairs.backend.domain.CombinationService;
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AssignmentConfig {

    @Bean
    // todo WIP
    public CombinationService assignmentService() {
        return new CombinationService() {
            @Override
            public List<ScoredCombination> calculate(List<Long> developerIds, List<Long> streamIds, int page) {
                return List.of();
            }
        };
    }
}
