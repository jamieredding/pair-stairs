package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.core.domain.Combination;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CombinationMapperTest {

    @Test
    void canMapEntityToCore() {
        CombinationEntity combinationEntity = new CombinationEntity(List.of(
                new PairStreamEntity(
                        List.of(new DeveloperEntity(0L, "", false), new DeveloperEntity(1L, "", false)),
                        new StreamEntity(10L, "")
                ),
                new PairStreamEntity(
                        List.of(new DeveloperEntity(2L, "", false)),
                        new StreamEntity(20L, "")
                )

        ));

        assertThat(CombinationMapper.entityToCore(combinationEntity))
                .isEqualTo(new Combination<>(Set.of(
                        new PairStream(Set.of("0", "1"), "10"),
                        new PairStream(Set.of("2"), "20")

                )));
    }

    @Test
    void canMapEntityToDomainAndSortDependenciesAlphabetically() {
        CombinationEntity entity = new CombinationEntity(List.of(
                new PairStreamEntity(
                        List.of(new DeveloperEntity(0L, "b", false), new DeveloperEntity(1L, "a", false)),
                        new StreamEntity(10L, "z")
                ),
                new PairStreamEntity(
                        List.of(new DeveloperEntity(2L, "c", false)),
                        new StreamEntity(20L, "y")
                )

        ));

        assertThat(CombinationMapper.entityToDomain(entity)).isEqualTo(
                List.of(
                        new dev.coldhands.pair.stairs.backend.domain.PairStream(
                                List.of(new DeveloperInfo(2L, "c", false)),
                                new StreamInfo(20L, "y")
                        ),
                        new dev.coldhands.pair.stairs.backend.domain.PairStream(
                                List.of(new DeveloperInfo(1L, "a", false), new DeveloperInfo(0L, "b", false)),
                                new StreamInfo(10L, "z")
                        )
                )
        );
    }

}