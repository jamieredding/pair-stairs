package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StreamMapperTest {

    @Test
    void canMapCoreStreamIdToAStreamInfoType() {
        final StreamMapper underTest = new StreamMapper(Map.of(0L, new StreamEntity(0L, "stream-a")));

        assertThat(underTest.coreToInfo("0")).isEqualTo(new StreamInfo(0L, "stream-a"));
    }
}