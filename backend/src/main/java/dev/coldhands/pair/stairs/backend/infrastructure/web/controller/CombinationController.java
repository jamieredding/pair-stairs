package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.CombinationService;
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/combinations")
public class CombinationController {

    private final CombinationService combinationService;

    @Autowired
    public CombinationController(CombinationService combinationService) {
        this.combinationService = combinationService;
    }

    @PostMapping("/calculate")
    List<ScoredCombination> calculate(@RequestParam("page") Optional<Integer> page,
                                      @RequestBody CalculateInputDto request) {
        final int requestedPage = page.orElse(0);

        final List<Long> developerIds = request.developerIds();
        final List<Long> streamIds = request.streamIds();

        return combinationService.calculate(developerIds, streamIds, requestedPage);
    }
}
