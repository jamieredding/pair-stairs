package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.FeatureFlagsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeatureFlagsController {

    @GetMapping("/api/v1/feature-flags")
    public FeatureFlagsDto getFeatureFlags(@Value("${app.feature.flag.teams.enabled}") boolean teamsEnabled) {
        return new FeatureFlagsDto(teamsEnabled);
    }

}
