package net.genie.member.service;

import java.util.function.Consumer;
import net.genie.member.domain.ChildrenByMother;
import net.genie.member.repository.ChildrenByMotherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link net.genie.member.domain.ChildrenByMother}.
 */
@Service
@Transactional
public class ChildrenByMotherService {

    private static final Logger LOG = LoggerFactory.getLogger(ChildrenByMotherService.class);

    private final ChildrenByMotherRepository childrenByMotherRepository;

    public ChildrenByMotherService(ChildrenByMotherRepository childrenByMotherRepository) {
        this.childrenByMotherRepository = childrenByMotherRepository;
    }

    /**
     * Save a childrenByMother.
     *
     * @param childrenByMother the entity to save.
     * @return the persisted entity.
     */
    public Mono<ChildrenByMother> save(ChildrenByMother childrenByMother) {
        LOG.debug("Request to save ChildrenByMother : {}", childrenByMother);
        return childrenByMotherRepository.save(childrenByMother);
    }

    /**
     * Update a childrenByMother.
     *
     * @param childrenByMother the entity to save.
     * @return the persisted entity.
     */
    public Mono<ChildrenByMother> update(ChildrenByMother childrenByMother) {
        LOG.debug("Request to update ChildrenByMother : {}", childrenByMother);
        return childrenByMotherRepository.save(childrenByMother);
    }

    /**
     * Partially update a childrenByMother.
     *
     * @param childrenByMother the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<ChildrenByMother> partialUpdate(ChildrenByMother childrenByMother) {
        LOG.debug("Request to partially update ChildrenByMother : {}", childrenByMother);

        return childrenByMotherRepository
            .findById(childrenByMother.getId())
            .map(existingChildrenByMother -> {
                updateIfPresent(existingChildrenByMother::setMotherId, childrenByMother.getMotherId());
                updateIfPresent(existingChildrenByMother::setChildId, childrenByMother.getChildId());
                updateIfPresent(existingChildrenByMother::setFatherId, childrenByMother.getFatherId());
                updateIfPresent(existingChildrenByMother::setFirstName, childrenByMother.getFirstName());
                updateIfPresent(existingChildrenByMother::setLastName, childrenByMother.getLastName());
                updateIfPresent(existingChildrenByMother::setGender, childrenByMother.getGender());
                updateIfPresent(existingChildrenByMother::setDob, childrenByMother.getDob());

                return existingChildrenByMother;
            })
            .flatMap(childrenByMotherRepository::save);
    }

    /**
     * Get all the childrenByMothers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<ChildrenByMother> findAll(Pageable pageable) {
        LOG.debug("Request to get all ChildrenByMothers");
        return childrenByMotherRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of childrenByMothers available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return childrenByMotherRepository.count();
    }

    /**
     * Get one childrenByMother by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<ChildrenByMother> findOne(Long id) {
        LOG.debug("Request to get ChildrenByMother : {}", id);
        return childrenByMotherRepository.findById(id);
    }

    /**
     * Delete the childrenByMother by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete ChildrenByMother : {}", id);
        return childrenByMotherRepository.deleteById(id);
    }

    private <T> void updateIfPresent(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
