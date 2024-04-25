package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.CalculateInput;
import dev.coldhands.pair.stairs.backend.domain.CombinationService;
import dev.coldhands.pair.stairs.backend.domain.Developer;
import dev.coldhands.pair.stairs.backend.domain.Stream;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.ScoredCombinationDto;
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
    List<ScoredCombinationDto> calculate(@RequestParam("page") Optional<Integer> page,
                                         @RequestBody CalculateInput calculateInput) {
        int requestedPage = page.orElse(0);

        final List<Developer> developers = calculateInput.developers();
        final List<Stream> streams = calculateInput.streams();

        return combinationService.calculate(developers, streams, requestedPage);
    }
}
