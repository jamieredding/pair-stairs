package dev.coldhands.pair.stairs.backend.infrastructure.wiring;

import dev.coldhands.pair.stairs.backend.domain.CombinationService;
import dev.coldhands.pair.stairs.backend.domain.Developer;
import dev.coldhands.pair.stairs.backend.domain.Stream;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ScoredCombinationDto;
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
            public List<ScoredCombinationDto> calculate(List<Developer> developers, List<Stream> streams, int page) {
                return List.of();
            }
        };
    }
}
