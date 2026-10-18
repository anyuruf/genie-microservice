package net.genie.member.repository;

import net.genie.member.domain.ChildrenByFather;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data R2DBC repository for the ChildrenByFather entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChildrenByFatherRepository extends R2dbcRepository<ChildrenByFather, Long> {
    Flux<ChildrenByFather> findAllBy(Pageable pageable);
}
