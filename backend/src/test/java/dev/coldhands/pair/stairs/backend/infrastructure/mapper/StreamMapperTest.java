package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StreamMapperTest {

    @Test
    void canMapCoreStreamIdToAStreamInfoType() {
        LookupById<StreamEntity> lookupById = _ -> new StreamEntity(0L, "stream-a");

        assertThat(StreamMapper.coreToInfo("0", lookupById)).isEqualTo(new StreamInfo(0L, "stream-a"));
    }

    @Test
    void canMapEntityToAStreamInfoType() {
        assertThat(StreamMapper.entityToInfo(new StreamEntity(0L, "stream-a"))).isEqualTo(new StreamInfo(0L, "stream-a"));
    }

    @Test
    void useEntityIdAsCoreStreamName() {
        assertThat(StreamMapper.entityToCore(new StreamEntity(0L, "stream-a"))).isEqualTo("0");
    }
}