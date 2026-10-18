package net.genie.member.web.rest;

import static net.genie.member.domain.ChildrenByFatherAsserts.*;
import static net.genie.member.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.genie.member.IntegrationTest;
import net.genie.member.domain.ChildrenByFather;
import net.genie.member.domain.enumeration.Gender;
import net.genie.member.repository.ChildrenByFatherRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import tools.jackson.databind.ObjectMapper;

/**
 * Integration tests for the {@link ChildrenByFatherResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class ChildrenByFatherResourceIT {

    private static final String DEFAULT_FATHER_ID = "AAAAAAAAAA";
    private static final String UPDATED_FATHER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_CHILD_ID = "AAAAAAAAAA";
    private static final String UPDATED_CHILD_ID = "BBBBBBBBBB";

    private static final String DEFAULT_MOTHER_ID = "AAAAAAAAAA";
    private static final String UPDATED_MOTHER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final Gender DEFAULT_GENDER = Gender.MALE;
    private static final Gender UPDATED_GENDER = Gender.FEMALE;

    private static final Instant DEFAULT_DOB = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DOB = Instant.ofEpochMilli(1703886261863L);

    private static final String ENTITY_API_URL = "/api/children-by-fathers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ChildrenByFatherRepository childrenByFatherRepository;

    @Autowired
    private R2dbcEntityTemplate template;

    @Autowired
    private WebTestClient webTestClient;

    private ChildrenByFather childrenByFather;

    private ChildrenByFather insertedChildrenByFather;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChildrenByFather createEntity() {
        return new ChildrenByFather()
            .fatherId(DEFAULT_FATHER_ID)
            .childId(DEFAULT_CHILD_ID)
            .motherId(DEFAULT_MOTHER_ID)
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .gender(DEFAULT_GENDER)
            .dob(DEFAULT_DOB);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChildrenByFather createUpdatedEntity() {
        return new ChildrenByFather()
            .fatherId(UPDATED_FATHER_ID)
            .childId(UPDATED_CHILD_ID)
            .motherId(UPDATED_MOTHER_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB);
    }

    public static void deleteEntities(R2dbcEntityTemplate template) {
        try {
            template.delete(ChildrenByFather.class).all().block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        childrenByFather = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedChildrenByFather != null) {
            childrenByFatherRepository.delete(insertedChildrenByFather).block();
            insertedChildrenByFather = null;
        }
        deleteEntities(template);
    }

    @Test
    void createChildrenByFather() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ChildrenByFather
        var returnedChildrenByFather = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ChildrenByFather.class)
            .returnResult()
            .getResponseBody();

        // Validate the ChildrenByFather in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertChildrenByFatherUpdatableFieldsEquals(returnedChildrenByFather, getPersistedChildrenByFather(returnedChildrenByFather));

        insertedChildrenByFather = returnedChildrenByFather;
    }

    @Test
    void createChildrenByFatherWithExistingId() throws Exception {
        // Create the ChildrenByFather with an existing ID
        childrenByFather.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByFather in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkFatherIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        childrenByFather.setFatherId(null);

        // Create the ChildrenByFather, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkChildIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        childrenByFather.setChildId(null);

        // Create the ChildrenByFather, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        childrenByFather.setFirstName(null);

        // Create the ChildrenByFather, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkLastNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        childrenByFather.setLastName(null);

        // Create the ChildrenByFather, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllChildrenByFathers() {
        // Initialize the database
        insertedChildrenByFather = childrenByFatherRepository.save(childrenByFather).block();

        // Get all the childrenByFatherList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(childrenByFather.getId().intValue()))
            .jsonPath("$.[*].fatherId")
            .value(hasItem(DEFAULT_FATHER_ID))
            .jsonPath("$.[*].childId")
            .value(hasItem(DEFAULT_CHILD_ID))
            .jsonPath("$.[*].motherId")
            .value(hasItem(DEFAULT_MOTHER_ID))
            .jsonPath("$.[*].firstName")
            .value(hasItem(DEFAULT_FIRST_NAME))
            .jsonPath("$.[*].lastName")
            .value(hasItem(DEFAULT_LAST_NAME))
            .jsonPath("$.[*].gender")
            .value(hasItem(DEFAULT_GENDER.toString()))
            .jsonPath("$.[*].dob")
            .value(hasItem(DEFAULT_DOB.toString()));
    }

    @Test
    void getChildrenByFather() {
        // Initialize the database
        insertedChildrenByFather = childrenByFatherRepository.save(childrenByFather).block();

        // Get the childrenByFather
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, childrenByFather.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(childrenByFather.getId().intValue()))
            .jsonPath("$.fatherId")
            .value(is(DEFAULT_FATHER_ID))
            .jsonPath("$.childId")
            .value(is(DEFAULT_CHILD_ID))
            .jsonPath("$.motherId")
            .value(is(DEFAULT_MOTHER_ID))
            .jsonPath("$.firstName")
            .value(is(DEFAULT_FIRST_NAME))
            .jsonPath("$.lastName")
            .value(is(DEFAULT_LAST_NAME))
            .jsonPath("$.gender")
            .value(is(DEFAULT_GENDER.toString()))
            .jsonPath("$.dob")
            .value(is(DEFAULT_DOB.toString()));
    }

    @Test
    void getNonExistingChildrenByFather() {
        // Get the childrenByFather
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingChildrenByFather() throws Exception {
        // Initialize the database
        insertedChildrenByFather = childrenByFatherRepository.save(childrenByFather).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the childrenByFather
        ChildrenByFather updatedChildrenByFather = childrenByFatherRepository.findById(childrenByFather.getId()).block();
        updatedChildrenByFather
            .fatherId(UPDATED_FATHER_ID)
            .childId(UPDATED_CHILD_ID)
            .motherId(UPDATED_MOTHER_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedChildrenByFather.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedChildrenByFather))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChildrenByFather in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedChildrenByFatherToMatchAllProperties(updatedChildrenByFather);
    }

    @Test
    void putNonExistingChildrenByFather() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByFather.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, childrenByFather.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByFather in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchChildrenByFather() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByFather.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByFather in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamChildrenByFather() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByFather.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ChildrenByFather in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateChildrenByFatherWithPatch() throws Exception {
        // Initialize the database
        insertedChildrenByFather = childrenByFatherRepository.save(childrenByFather).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the childrenByFather using partial update
        ChildrenByFather partialUpdatedChildrenByFather = new ChildrenByFather();
        partialUpdatedChildrenByFather.setId(childrenByFather.getId());

        partialUpdatedChildrenByFather
            .fatherId(UPDATED_FATHER_ID)
            .childId(UPDATED_CHILD_ID)
            .firstName(UPDATED_FIRST_NAME)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedChildrenByFather.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedChildrenByFather))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChildrenByFather in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChildrenByFatherUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedChildrenByFather, childrenByFather),
            getPersistedChildrenByFather(childrenByFather)
        );
    }

    @Test
    void fullUpdateChildrenByFatherWithPatch() throws Exception {
        // Initialize the database
        insertedChildrenByFather = childrenByFatherRepository.save(childrenByFather).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the childrenByFather using partial update
        ChildrenByFather partialUpdatedChildrenByFather = new ChildrenByFather();
        partialUpdatedChildrenByFather.setId(childrenByFather.getId());

        partialUpdatedChildrenByFather
            .fatherId(UPDATED_FATHER_ID)
            .childId(UPDATED_CHILD_ID)
            .motherId(UPDATED_MOTHER_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedChildrenByFather.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedChildrenByFather))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChildrenByFather in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChildrenByFatherUpdatableFieldsEquals(
            partialUpdatedChildrenByFather,
            getPersistedChildrenByFather(partialUpdatedChildrenByFather)
        );
    }

    @Test
    void patchNonExistingChildrenByFather() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByFather.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, childrenByFather.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByFather in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchChildrenByFather() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByFather.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByFather in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamChildrenByFather() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByFather.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(childrenByFather))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ChildrenByFather in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteChildrenByFather() {
        // Initialize the database
        insertedChildrenByFather = childrenByFatherRepository.save(childrenByFather).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the childrenByFather
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, childrenByFather.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return childrenByFatherRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ChildrenByFather getPersistedChildrenByFather(ChildrenByFather childrenByFather) {
        return childrenByFatherRepository.findById(childrenByFather.getId()).block();
    }

    protected void assertPersistedChildrenByFatherToMatchAllProperties(ChildrenByFather expectedChildrenByFather) {
        // Test fails because reactive api returns an empty object instead of null
        // assertChildrenByFatherAllPropertiesEquals(expectedChildrenByFather, getPersistedChildrenByFather(expectedChildrenByFather));
        assertChildrenByFatherUpdatableFieldsEquals(expectedChildrenByFather, getPersistedChildrenByFather(expectedChildrenByFather));
    }

    protected void assertPersistedChildrenByFatherToMatchUpdatableProperties(ChildrenByFather expectedChildrenByFather) {
        // Test fails because reactive api returns an empty object instead of null
        // assertChildrenByFatherAllUpdatablePropertiesEquals(expectedChildrenByFather, getPersistedChildrenByFather(expectedChildrenByFather));
        assertChildrenByFatherUpdatableFieldsEquals(expectedChildrenByFather, getPersistedChildrenByFather(expectedChildrenByFather));
    }
}
