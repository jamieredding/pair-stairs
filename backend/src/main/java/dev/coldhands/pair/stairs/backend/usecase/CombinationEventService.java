package dev.coldhands.pair.stairs.backend.usecase;

import dev.coldhands.pair.stairs.backend.domain.CombinationEvent;
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.CombinationEventMapper;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.*;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.*;
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class CombinationEventService {

    private final DeveloperRepository developerRepository;
    private final StreamRepository streamRepository;
    private final PairStreamRepository pairStreamRepository;
    private final CombinationRepository combinationRepository;
    private final CombinationEventRepository combinationEventRepository;

    public CombinationEventService(DeveloperRepository developerRepository, StreamRepository streamRepository, PairStreamRepository pairStreamRepository, CombinationRepository combinationRepository, CombinationEventRepository combinationEventRepository) {
        this.developerRepository = developerRepository;
        this.streamRepository = streamRepository;
        this.pairStreamRepository = pairStreamRepository;
        this.combinationRepository = combinationRepository;
        this.combinationEventRepository = combinationEventRepository;
    }

    public List<CombinationEvent> getCombinationEvents(int requestedPage, int pageSize) {
        final PageRequest pageRequest = PageRequest.of(requestedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"));

        return combinationEventRepository.findAll(pageRequest).stream()
                .map(CombinationEventMapper::entityToDomain)
                .toList();
    }

    // todo exposing the Dto here seems wrong
    public void saveEvent(LocalDate date, List<SaveCombinationEventDto.PairStreamByIds> combinationByIds) {
        // todo validate all ids actually exist

        record AllIds(List<Long> developerIds, List<Long> streamIds) {
        }

        final AllIds allIds = combinationByIds.stream()
                .collect(teeing(
                        flatMapping(ids -> ids.developerIds().stream(), toList()),
                        mapping(SaveCombinationEventDto.PairStreamByIds::streamId, toList()),
                        AllIds::new
                ));

        final Map<Long, DeveloperEntity> developersById = developerRepository.findAllById(allIds.developerIds).stream()
                .collect(toMap(DeveloperEntity::getId, Function.identity()));

        final Map<Long, StreamEntity> streamsById = streamRepository.findAllById(allIds.streamIds).stream()
                .collect(toMap(StreamEntity::getId, Function.identity()));

        final List<PairStreamEntity> pairStreamEntities = combinationByIds.stream()
                .map(ids -> findOrCreatePairStreamEntity(ids, developersById, streamsById))
                .toList();

        final CombinationEntity combination = findOrCreateCombinationEntity(pairStreamEntities);

        combinationEventRepository.save(new CombinationEventEntity(date, combination));
    }

    public void deleteEvent(long id) {
        if (!combinationEventRepository.existsById(id)) {
            throw new EntityNotFoundException();
        }
        combinationEventRepository.deleteById(id);
    }

    private PairStreamEntity findOrCreatePairStreamEntity(SaveCombinationEventDto.PairStreamByIds ids, Map<Long, DeveloperEntity> developersById, Map<Long, StreamEntity> streamsById) {
        final List<DeveloperEntity> developerEntities = ids.developerIds().stream()
                .map(developersById::get)
                .toList();

        final StreamEntity streamEntity = streamsById.get(ids.streamId());

        return pairStreamRepository.findByDevelopersAndStream(ids.developerIds(), streamEntity, ids.developerIds().size()).stream()
                .findFirst()
                .orElseGet(() -> pairStreamRepository.save(new PairStreamEntity(developerEntities, streamEntity)));
    }

    private CombinationEntity findOrCreateCombinationEntity(List<PairStreamEntity> pairStreamEntities) {
        final List<Long> pairStreamIds = pairStreamEntities.stream()
                .map(PairStreamEntity::getId)
                .toList();
        return combinationRepository.findByPairStreams(pairStreamIds, pairStreamIds.size()).stream()
                .findFirst()
                .orElseGet(() -> combinationRepository.save(new CombinationEntity(pairStreamEntities)));
    }
}
