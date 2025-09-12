package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CreateTeamDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ErrorDto;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/teams")
@ConditionalOnBooleanProperty("app.feature.flag.teams.enabled")
public class TeamController {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]+$");
    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @PostMapping
    public ResponseEntity<?> saveTeam(@RequestBody CreateTeamDto createTeamDto) {
        final String errorMessage = validate(createTeamDto);
        if (errorMessage != null) {
            return ResponseEntity.badRequest().body(new ErrorDto(errorMessage));
        }

        if (teamRepository.findBySlug(createTeamDto.slug()) != null) {
            return ResponseEntity.badRequest().body(new ErrorDto("DUPLICATE_SLUG"));
        }

        final var teamEntity = teamRepository.save(new TeamEntity(createTeamDto.name(), createTeamDto.slug()));

        return ResponseEntity.status(201)
                .body(new TeamDto(teamEntity.getId(), teamEntity.getName(), teamEntity.getSlug()));
    }

    private String validate(CreateTeamDto createTeamDto) {
        final var name = createTeamDto.name();
        if (name == null || name.isBlank()) {
            return "INVALID_NAME";
        }
        final var slug = createTeamDto.slug();
        if (slug == null || slug.isBlank() || !SLUG_PATTERN.matcher(slug).matches()) {
            return "INVALID_SLUG";
        }
        return null;
    }

}
