package net.genie.member.service;

import java.util.function.Consumer;
import net.genie.member.domain.Member;
import net.genie.member.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link net.genie.member.domain.Member}.
 */
@Service
@Transactional
public class MemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Save a member.
     *
     * @param member the entity to save.
     * @return the persisted entity.
     */
    public Mono<Member> save(Member member) {
        LOG.debug("Request to save Member : {}", member);
        return memberRepository.save(member);
    }

    /**
     * Update a member.
     *
     * @param member the entity to save.
     * @return the persisted entity.
     */
    public Mono<Member> update(Member member) {
        LOG.debug("Request to update Member : {}", member);
        return memberRepository.save(member.setIsPersisted());
    }

    /**
     * Partially update a member.
     *
     * @param member the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Member> partialUpdate(Member member) {
        LOG.debug("Request to partially update Member : {}", member);

        return memberRepository
            .findById(member.getId())
            .map(existingMember -> {
                updateIfPresent(existingMember::setFirstName, member.getFirstName());
                updateIfPresent(existingMember::setLastName, member.getLastName());
                updateIfPresent(existingMember::setDescription, member.getDescription());
                updateIfPresent(existingMember::setGender, member.getGender());
                updateIfPresent(existingMember::setDob, member.getDob());
                updateIfPresent(existingMember::setImageUrl, member.getImageUrl());

                return existingMember;
            })
            .flatMap(memberRepository::save);
    }

    /**
     * Get all the members.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Member> findAll(Pageable pageable) {
        LOG.debug("Request to get all Members");
        return memberRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of members available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return memberRepository.count();
    }

    /**
     * Get one member by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Member> findOne(String id) {
        LOG.debug("Request to get Member : {}", id);
        return memberRepository.findById(id);
    }

    /**
     * Delete the member by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        LOG.debug("Request to delete Member : {}", id);
        return memberRepository.deleteById(id);
    }

    private <T> void updateIfPresent(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
