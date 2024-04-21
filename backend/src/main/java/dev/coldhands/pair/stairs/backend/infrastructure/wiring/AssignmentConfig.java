package dev.coldhands.pair.stairs.backend.infrastructure.wiring;

import dev.coldhands.pair.stairs.backend.domain.AssignmentService;
import dev.coldhands.pair.stairs.backend.domain.Developer;
import dev.coldhands.pair.stairs.backend.domain.ScoredAssignment;
import dev.coldhands.pair.stairs.backend.domain.Stream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AssignmentConfig {

    @Bean
    // todo WIP
    public AssignmentService assignmentService() {
        return new AssignmentService() {
            @Override
            public List<ScoredAssignment> calculate(List<Developer> developers, List<Stream> streams, int page) {
                return List.of();
            }
        };
    }
}
