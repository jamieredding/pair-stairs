package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.DeveloperMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/developers")
public class DeveloperController {

    private final DeveloperRepository repository;

    @Autowired
    public DeveloperController(DeveloperRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<DeveloperEntity> getDevelopers() {
        return repository.findAll();
    }

    @GetMapping("/info")
    public List<DeveloperInfo> getDeveloperInfos() {
        final List<DeveloperEntity> developerEntities = repository.findAll();

        return developerEntities.stream()
                .map(DeveloperMapper::entityToInfo)
                .toList();
    }

    @PostMapping
    public ResponseEntity<DeveloperEntity> saveDeveloper(@RequestBody DeveloperEntity developer) {
        final DeveloperEntity savedDeveloper = repository.save(developer);

        return ResponseEntity.status(201)
                .body(savedDeveloper);
    }
}
