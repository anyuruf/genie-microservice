package net.genie.member.service;

import java.util.function.Consumer;
import net.genie.member.domain.ChildrenByFather;
import net.genie.member.repository.ChildrenByFatherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link net.genie.member.domain.ChildrenByFather}.
 */
@Service
@Transactional
public class ChildrenByFatherService {

    private static final Logger LOG = LoggerFactory.getLogger(ChildrenByFatherService.class);

    private final ChildrenByFatherRepository childrenByFatherRepository;

    public ChildrenByFatherService(ChildrenByFatherRepository childrenByFatherRepository) {
        this.childrenByFatherRepository = childrenByFatherRepository;
    }

    /**
     * Save a childrenByFather.
     *
     * @param childrenByFather the entity to save.
     * @return the persisted entity.
     */
    public Mono<ChildrenByFather> save(ChildrenByFather childrenByFather) {
        LOG.debug("Request to save ChildrenByFather : {}", childrenByFather);
        return childrenByFatherRepository.save(childrenByFather);
    }

    /**
     * Update a childrenByFather.
     *
     * @param childrenByFather the entity to save.
     * @return the persisted entity.
     */
    public Mono<ChildrenByFather> update(ChildrenByFather childrenByFather) {
        LOG.debug("Request to update ChildrenByFather : {}", childrenByFather);
        return childrenByFatherRepository.save(childrenByFather);
    }

    /**
     * Partially update a childrenByFather.
     *
     * @param childrenByFather the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<ChildrenByFather> partialUpdate(ChildrenByFather childrenByFather) {
        LOG.debug("Request to partially update ChildrenByFather : {}", childrenByFather);

        return childrenByFatherRepository
            .findById(childrenByFather.getId())
            .map(existingChildrenByFather -> {
                updateIfPresent(existingChildrenByFather::setFatherId, childrenByFather.getFatherId());
                updateIfPresent(existingChildrenByFather::setChildId, childrenByFather.getChildId());
                updateIfPresent(existingChildrenByFather::setMotherId, childrenByFather.getMotherId());
                updateIfPresent(existingChildrenByFather::setFirstName, childrenByFather.getFirstName());
                updateIfPresent(existingChildrenByFather::setLastName, childrenByFather.getLastName());
                updateIfPresent(existingChildrenByFather::setGender, childrenByFather.getGender());
                updateIfPresent(existingChildrenByFather::setDob, childrenByFather.getDob());

                return existingChildrenByFather;
            })
            .flatMap(childrenByFatherRepository::save);
    }

    /**
     * Get all the childrenByFathers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<ChildrenByFather> findAll(Pageable pageable) {
        LOG.debug("Request to get all ChildrenByFathers");
        return childrenByFatherRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of childrenByFathers available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return childrenByFatherRepository.count();
    }

    /**
     * Get one childrenByFather by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<ChildrenByFather> findOne(Long id) {
        LOG.debug("Request to get ChildrenByFather : {}", id);
        return childrenByFatherRepository.findById(id);
    }

    /**
     * Delete the childrenByFather by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete ChildrenByFather : {}", id);
        return childrenByFatherRepository.deleteById(id);
    }

    private <T> void updateIfPresent(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
