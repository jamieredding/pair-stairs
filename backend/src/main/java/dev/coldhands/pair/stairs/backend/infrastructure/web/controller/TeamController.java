package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
public class TeamController {

    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @PostMapping
    public ResponseEntity<?> saveTeam(@Valid @RequestBody CreateTeamDto createTeamDto) {
        if (teamRepository.findBySlug(createTeamDto.slug()) != null) {
            return ResponseEntity.badRequest().body(new ErrorDto("DUPLICATE_SLUG"));
        }

        final var teamEntity = teamRepository.save(new TeamEntity(createTeamDto.name(), createTeamDto.slug()));

        return ResponseEntity.status(201)
                .body(new TeamDto(teamEntity.getId(), teamEntity.getName(), teamEntity.getSlug()));
    }

}
