package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.Developer;
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
    public List<Developer> getDevelopers() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Developer> saveDeveloper(@RequestBody Developer developer) {
        final Developer savedDeveloper = repository.save(developer);

        return ResponseEntity.status(201)
                .body(savedDeveloper);
    }
}
