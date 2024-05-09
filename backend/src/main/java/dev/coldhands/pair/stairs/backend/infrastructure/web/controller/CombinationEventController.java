package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.CombinationEvent;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/combinations/event")
public class CombinationEventController {

    private final CombinationEventService combinationEventService;
    private final int pageSize;

    @Autowired
    public CombinationEventController(CombinationEventService combinationEventService,
                                      @Value("${app.combinations.event.pageSize}") int pageSize) {
        this.combinationEventService = combinationEventService;
        this.pageSize = pageSize;
    }

    @GetMapping
    ResponseEntity<List<CombinationEvent>> getCombinationEvents(@RequestParam("page") Optional<Integer> page) {
        final int requestedPage = page.orElse(0);

        return ResponseEntity.ok(combinationEventService.getCombinationEvents(requestedPage, pageSize));
    }

    @PostMapping
    ResponseEntity<Void> saveEvent(@RequestBody SaveCombinationEventDto request) {
        combinationEventService.saveEvent(request.date(), request.combination());

        return ResponseEntity.status(201)
                .build();
    }
}
