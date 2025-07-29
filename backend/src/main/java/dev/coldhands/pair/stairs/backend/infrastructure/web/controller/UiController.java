package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.infrastructure.mapper.DeveloperMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.DeveloperRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UiController {

    private final DeveloperRepository developerRepository;

    public UiController(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    @GetMapping("/developers")
    public String developers(Model model) {
        final List<DeveloperEntity> entities = developerRepository.findAll();

        model.addAttribute("developerInfos", entities.stream()
                .map(DeveloperMapper::entityToInfo)
                .toList());

        return "developers";
    }
}
