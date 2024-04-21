package dev.coldhands.pair.stairs.backend.infrastructure.web;

import dev.coldhands.pair.stairs.backend.domain.Stream;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.StreamRepository;
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
    public List<Stream> getStreams() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Stream> saveStream(@RequestBody Stream stream) {
        final Stream savedStream = repository.save(stream);

        return ResponseEntity.status(201)
                .body(savedStream);
    }
}
