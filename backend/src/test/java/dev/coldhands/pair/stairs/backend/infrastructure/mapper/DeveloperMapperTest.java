package dev.coldhands.pair.stairs.backend.infrastructure.mapper;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeveloperMapperTest {

    @Test
    void canMapCoreDeveloperIdToADeveloperInfoType() {
        LookupById<DeveloperEntity> lookupById = _ -> new DeveloperEntity(0L, "dev-0", true);

        assertThat(DeveloperMapper.coreToInfo("0", lookupById)).isEqualTo(new DeveloperInfo(0L, "dev-0", true));
    }

    @Test
    void canMapEntityToInfo() {
        assertThat(DeveloperMapper.entityToInfo(new DeveloperEntity(0L, "dev-0", true))).isEqualTo(new DeveloperInfo(0L, "dev-0", true));
    }

    @Test
    void useEntityIdAsCoreDeveloperName() {
        assertThat(DeveloperMapper.entityToCore(new DeveloperEntity(0L, "dev-0", true))).isEqualTo("0");
    }
}