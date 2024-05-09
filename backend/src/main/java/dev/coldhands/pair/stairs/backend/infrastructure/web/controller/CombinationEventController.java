package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.CombinationEvent;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/combinations/event")
public class CombinationEventController {

    private final CombinationEventService combinationEventService;

    @Autowired
    public CombinationEventController(CombinationEventService combinationEventService) {
        this.combinationEventService = combinationEventService;
    }

    @GetMapping
    ResponseEntity<List<CombinationEvent>> getCombinationEvents() {
        return ResponseEntity.ok(combinationEventService.getCombinationEvents());
    }

    @PostMapping
    ResponseEntity<Void> saveEvent(@RequestBody SaveCombinationEventDto request) {
        combinationEventService.saveEvent(request.date(), request.combination());

        return ResponseEntity.status(201)
                .build();
    }
}
