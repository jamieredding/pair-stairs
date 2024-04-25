package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.StreamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/streams")
public class StreamController {

    private final StreamRepository repository;

    @Autowired
    public StreamController(StreamRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<StreamEntity> getStreams() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<StreamEntity> saveStream(@RequestBody StreamEntity stream) {
        final StreamEntity savedStream = repository.save(stream);

        return ResponseEntity.status(201)
                .body(savedStream);
    }
}
