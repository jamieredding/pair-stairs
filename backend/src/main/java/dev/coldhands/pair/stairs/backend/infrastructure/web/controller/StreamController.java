package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.StreamInfo;
import dev.coldhands.pair.stairs.backend.domain.stream.StreamStats;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.StreamMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.PatchStreamDto;
import dev.coldhands.pair.stairs.backend.usecase.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/streams")
public class StreamController {

    private final StreamRepository repository;
    private final StatsService statsService;

    @Autowired
    public StreamController(StreamRepository repository, StatsService statsService) {
        this.repository = repository;
        this.statsService = statsService;
    }

    @GetMapping
    public List<StreamEntity> getStreams() {
        return repository.findAll();
    }

    @GetMapping("/info")
    public List<StreamInfo> getStreamInfos() {
        final List<StreamEntity> streamEntities = repository.findAll();

        return streamEntities.stream()
                .map(StreamMapper::entityToInfo)
                .toList();
    }

    @PostMapping
    public ResponseEntity<StreamEntity> saveStream(@RequestBody StreamEntity stream) {
        final StreamEntity savedStream = repository.save(stream);

        return ResponseEntity.status(201)
                .body(savedStream);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<StreamEntity> updateStream(@RequestBody PatchStreamDto dto, @PathVariable("id") long id) {
        return repository.findById(id)
                .map(stream -> {
                    stream.setArchived(dto.archived());
                    return repository.save(stream);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<StreamStats> getStats(@PathVariable("id") long id,
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
                                ? statsService.getStreamStatsBetween(id, requestedStartDate.get(), requestedEndDate.get())
                                : statsService.getStreamStats(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
