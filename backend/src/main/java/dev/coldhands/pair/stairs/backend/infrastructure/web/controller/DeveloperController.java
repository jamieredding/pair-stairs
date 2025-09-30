package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo;
import dev.coldhands.pair.stairs.backend.domain.DeveloperStats;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.DeveloperMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.PatchDeveloperDto;
import dev.coldhands.pair.stairs.backend.usecase.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/developers")
public class DeveloperController {

    private final DeveloperRepository repository;
    private final StatsService statsService;

    @Autowired
    public DeveloperController(DeveloperRepository repository,
                               StatsService statsService) {
        this.repository = repository;
        this.statsService = statsService;
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

    @PatchMapping("/{id}")
    public ResponseEntity<DeveloperEntity> updateDeveloper(@RequestBody PatchDeveloperDto dto, @PathVariable("id") long id) {
        return repository.findById(id)
                .map(developer -> {
                    developer.setArchived(dto.archived());
                    return repository.save(developer);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<DeveloperStats> getStats(@PathVariable("id") long id,
                                                   @RequestParam("startDate") Optional<LocalDate> requestedStartDate,
                                                   @RequestParam("endDate") Optional<LocalDate> requestedEndDate) {
        if ((requestedStartDate.isPresent() && requestedEndDate.isEmpty()) ||
                (requestedStartDate.isEmpty() && requestedEndDate.isPresent()) ||
                (requestedStartDate.isPresent() && requestedStartDate.get().isAfter(requestedEndDate.get()))) {
            return ResponseEntity.badRequest().build();
        }

        return repository.findById(id)
                .map(_ ->
                        requestedStartDate.isPresent()
                                ? statsService.getDeveloperStatsBetween(id, requestedStartDate.get(), requestedEndDate.get())
                                : statsService.getDeveloperStats(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
