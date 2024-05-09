package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.domain.CombinationCalculationService;
import dev.coldhands.pair.stairs.backend.domain.ScoredCombination;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.CalculateInputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/combinations/calculate")
public class CombinationCalculationController {

    private final CombinationCalculationService service;
    private final int pageSize;

    @Autowired
    public CombinationCalculationController(CombinationCalculationService service,
                                            @Value("${app.combinations.calculate.pageSize}") int pageSize) {
        this.service = service;
        this.pageSize = pageSize;
    }

    @PostMapping
    List<ScoredCombination> calculate(@RequestParam("page") Optional<Integer> page,
                                      @RequestBody CalculateInputDto request) {
        final int requestedPage = page.orElse(0);

        final List<Long> developerIds = request.developerIds();
        final List<Long> streamIds = request.streamIds();

        return service.calculate(developerIds, streamIds, requestedPage, pageSize);
    }
}
