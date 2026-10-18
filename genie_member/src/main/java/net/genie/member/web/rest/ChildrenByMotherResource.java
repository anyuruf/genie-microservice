package net.genie.member.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import net.genie.member.domain.ChildrenByMother;
import net.genie.member.repository.ChildrenByMotherRepository;
import net.genie.member.service.ChildrenByMotherService;
import net.genie.member.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link net.genie.member.domain.ChildrenByMother}.
 */
@RestController
@RequestMapping("/api/children-by-mothers")
public class ChildrenByMotherResource {

    private static final Logger LOG = LoggerFactory.getLogger(ChildrenByMotherResource.class);

    private static final String ENTITY_NAME = "genieMemberChildrenByMother";

    @Value("${jhipster.clientApp.name:geniemember}")
    private String applicationName;

    private final ChildrenByMotherService childrenByMotherService;

    private final ChildrenByMotherRepository childrenByMotherRepository;

    public ChildrenByMotherResource(
        ChildrenByMotherService childrenByMotherService,
        ChildrenByMotherRepository childrenByMotherRepository
    ) {
        this.childrenByMotherService = childrenByMotherService;
        this.childrenByMotherRepository = childrenByMotherRepository;
    }

    /**
     * {@code POST  /children-by-mothers} : Create a new childrenByMother.
     *
     * @param childrenByMother the childrenByMother to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new childrenByMother, or with status {@code 400 (Bad Request)} if the childrenByMother already has an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<ChildrenByMother>> createChildrenByMother(@Valid @RequestBody ChildrenByMother childrenByMother)
        throws URISyntaxException {
        LOG.debug("REST request to save ChildrenByMother : {}", childrenByMother);
        if (childrenByMother.getId() != null) {
            throw new BadRequestAlertException("A new childrenByMother cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return childrenByMotherService.save(childrenByMother).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/children-by-mothers/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * {@code PUT  /children-by-mothers/:id} : Updates an existing childrenByMother.
     *
     * @param id the id of the childrenByMother to save.
     * @param childrenByMother the childrenByMother to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated childrenByMother,
     * or with status {@code 400 (Bad Request)} if the childrenByMother is not valid,
     * or with status {@code 500 (Internal Server Error)} if the childrenByMother couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ChildrenByMother>> updateChildrenByMother(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ChildrenByMother childrenByMother
    ) throws URISyntaxException {
        LOG.debug("REST request to update ChildrenByMother : {}, {}", id, childrenByMother);
        if (childrenByMother.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, childrenByMother.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return childrenByMotherRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return childrenByMotherService
                .update(childrenByMother)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
    }

    /**
     * {@code PATCH  /children-by-mothers/:id} : Partial updates given fields of an existing childrenByMother, field will ignore if it is null
     *
     * @param id the id of the childrenByMother to save.
     * @param childrenByMother the childrenByMother to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated childrenByMother,
     * or with status {@code 400 (Bad Request)} if the childrenByMother is not valid,
     * or with status {@code 404 (Not Found)} if the childrenByMother is not found,
     * or with status {@code 500 (Internal Server Error)} if the childrenByMother couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<ChildrenByMother>> partialUpdateChildrenByMother(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ChildrenByMother childrenByMother
    ) throws URISyntaxException {
        LOG.debug("REST request to partially update ChildrenByMother : {}, {}", id, childrenByMother);
        if (childrenByMother.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, childrenByMother.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return childrenByMotherRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<ChildrenByMother> result = childrenByMotherService.partialUpdate(childrenByMother);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /children-by-mothers} : get all the Children By Mothers.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Children By Mothers in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<ChildrenByMother>>> getAllChildrenByMothers(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get a page of ChildrenByMothers");
        return childrenByMotherService
            .countAll()
            .zipWith(childrenByMotherService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity.ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /children-by-mothers/:id} : get the "id" childrenByMother.
     *
     * @param id the id of the childrenByMother to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the childrenByMother, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ChildrenByMother>> getChildrenByMother(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ChildrenByMother : {}", id);
        Mono<ChildrenByMother> childrenByMother = childrenByMotherService.findOne(id);
        return ResponseUtil.wrapOrNotFound(childrenByMother);
    }

    /**
     * {@code DELETE  /children-by-mothers/:id} : delete the "id" childrenByMother.
     *
     * @param id the id of the childrenByMother to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteChildrenByMother(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ChildrenByMother : {}", id);
        return childrenByMotherService
            .delete(id)

            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
