package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeveloperMapperTest {

    @Test
    void canMapCoreDeveloperIdToADeveloperInfoType() {
        final DeveloperMapper underTest = new DeveloperMapper(Map.of(0L, new DeveloperEntity(0L, "dev-0")));

        assertThat(underTest.coreToInfo("0")).isEqualTo(new DeveloperInfo(0L, "dev-0"));
    }
}