package net.genie.member.repository;

import net.genie.member.domain.ChildrenByMother;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data R2DBC repository for the ChildrenByMother entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChildrenByMotherRepository extends R2dbcRepository<ChildrenByMother, Long> {
    Flux<ChildrenByMother> findAllBy(Pageable pageable);
}
