package net.genie.member.repository;

import net.genie.member.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data R2DBC repository for the Member entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MemberRepository extends R2dbcRepository<Member, String> {
    Flux<Member> findAllBy(Pageable pageable);
}
