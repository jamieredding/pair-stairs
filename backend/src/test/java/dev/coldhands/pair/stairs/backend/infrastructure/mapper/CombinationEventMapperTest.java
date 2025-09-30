package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.CombinationEvent;
import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.PairStream;
import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CombinationEventMapperTest {

    @Nested
    class EntityToDomain {

        @Test
        void canMapAndSortDependenciesAlphabetically() {
            CombinationEventEntity entity = new CombinationEventEntity(LocalDate.of(2024, 5, 6),
                    new CombinationEntity(List.of(
                            new PairStreamEntity(
                                    List.of(new DeveloperEntity(0L, "b", false), new DeveloperEntity(1L, "a", false)),
                                    new StreamEntity(10L, "z")
                            ),
                            new PairStreamEntity(
                                    List.of(new DeveloperEntity(2L, "c", false)),
                                    new StreamEntity(20L, "y")
                            )

                    )));
            entity.setId(1L);

            assertThat(CombinationEventMapper.entityToDomain(entity)).isEqualTo(
                    new CombinationEvent(1L,
                            LocalDate.of(2024, 5, 6),
                            List.of(
                                    new PairStream(
                                            List.of(new DeveloperInfo(2L, "c", false)),
                                            new StreamInfo(20L, "y")
                                    ),
                                    new PairStream(
                                            List.of(new DeveloperInfo(1L, "a", false), new DeveloperInfo(0L, "b", false)),
                                            new StreamInfo(10L, "z")
                                    )
                            ))
            );
        }
    }
}