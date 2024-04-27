package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

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
                        List.of(new DeveloperEntity(0L, ""), new DeveloperEntity(1L, "")),
                        new StreamEntity(10L, "")
                ),
                new PairStreamEntity(
                        List.of(new DeveloperEntity(2L, "")),
                        new StreamEntity(20L, "")
                )

        ));

        assertThat(CombinationMapper.entityToCore(combinationEntity))
                .isEqualTo(new Combination<>(Set.of(
                        new PairStream(Set.of("0", "1"), "10"),
                        new PairStream(Set.of("2"), "20")

                )));
    }
}