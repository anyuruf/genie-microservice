package net.genie.member.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import net.genie.member.domain.ChildrenByFather;
import net.genie.member.repository.ChildrenByFatherRepository;
import net.genie.member.service.ChildrenByFatherService;
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
 * REST controller for managing {@link net.genie.member.domain.ChildrenByFather}.
 */
@RestController
@RequestMapping("/api/children-by-fathers")
public class ChildrenByFatherResource {

    private static final Logger LOG = LoggerFactory.getLogger(ChildrenByFatherResource.class);

    private static final String ENTITY_NAME = "genieMemberChildrenByFather";

    @Value("${jhipster.clientApp.name:geniemember}")
    private String applicationName;

    private final ChildrenByFatherService childrenByFatherService;

    private final ChildrenByFatherRepository childrenByFatherRepository;

    public ChildrenByFatherResource(
        ChildrenByFatherService childrenByFatherService,
        ChildrenByFatherRepository childrenByFatherRepository
    ) {
        this.childrenByFatherService = childrenByFatherService;
        this.childrenByFatherRepository = childrenByFatherRepository;
    }

    /**
     * {@code POST  /children-by-fathers} : Create a new childrenByFather.
     *
     * @param childrenByFather the childrenByFather to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new childrenByFather, or with status {@code 400 (Bad Request)} if the childrenByFather already has an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<ChildrenByFather>> createChildrenByFather(@Valid @RequestBody ChildrenByFather childrenByFather)
        throws URISyntaxException {
        LOG.debug("REST request to save ChildrenByFather : {}", childrenByFather);
        if (childrenByFather.getId() != null) {
            throw new BadRequestAlertException("A new childrenByFather cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return childrenByFatherService.save(childrenByFather).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/children-by-fathers/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * {@code PUT  /children-by-fathers/:id} : Updates an existing childrenByFather.
     *
     * @param id the id of the childrenByFather to save.
     * @param childrenByFather the childrenByFather to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated childrenByFather,
     * or with status {@code 400 (Bad Request)} if the childrenByFather is not valid,
     * or with status {@code 500 (Internal Server Error)} if the childrenByFather couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ChildrenByFather>> updateChildrenByFather(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ChildrenByFather childrenByFather
    ) throws URISyntaxException {
        LOG.debug("REST request to update ChildrenByFather : {}, {}", id, childrenByFather);
        if (childrenByFather.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, childrenByFather.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return childrenByFatherRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return childrenByFatherService
                .update(childrenByFather)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
    }

    /**
     * {@code PATCH  /children-by-fathers/:id} : Partial updates given fields of an existing childrenByFather, field will ignore if it is null
     *
     * @param id the id of the childrenByFather to save.
     * @param childrenByFather the childrenByFather to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated childrenByFather,
     * or with status {@code 400 (Bad Request)} if the childrenByFather is not valid,
     * or with status {@code 404 (Not Found)} if the childrenByFather is not found,
     * or with status {@code 500 (Internal Server Error)} if the childrenByFather couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<ChildrenByFather>> partialUpdateChildrenByFather(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ChildrenByFather childrenByFather
    ) throws URISyntaxException {
        LOG.debug("REST request to partially update ChildrenByFather : {}, {}", id, childrenByFather);
        if (childrenByFather.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, childrenByFather.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return childrenByFatherRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<ChildrenByFather> result = childrenByFatherService.partialUpdate(childrenByFather);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /children-by-fathers} : get all the Children By Fathers.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Children By Fathers in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<ChildrenByFather>>> getAllChildrenByFathers(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get a page of ChildrenByFathers");
        return childrenByFatherService
            .countAll()
            .zipWith(childrenByFatherService.findAll(pageable).collectList())
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
     * {@code GET  /children-by-fathers/:id} : get the "id" childrenByFather.
     *
     * @param id the id of the childrenByFather to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the childrenByFather, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ChildrenByFather>> getChildrenByFather(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ChildrenByFather : {}", id);
        Mono<ChildrenByFather> childrenByFather = childrenByFatherService.findOne(id);
        return ResponseUtil.wrapOrNotFound(childrenByFather);
    }

    /**
     * {@code DELETE  /children-by-fathers/:id} : delete the "id" childrenByFather.
     *
     * @param id the id of the childrenByFather to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteChildrenByFather(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ChildrenByFather : {}", id);
        return childrenByFatherService
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
