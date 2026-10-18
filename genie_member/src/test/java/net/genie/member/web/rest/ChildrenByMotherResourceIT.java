package net.genie.member.web.rest;

import static net.genie.member.domain.ChildrenByMotherAsserts.*;
import static net.genie.member.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.genie.member.IntegrationTest;
import net.genie.member.domain.ChildrenByMother;
import net.genie.member.domain.enumeration.Gender;
import net.genie.member.repository.ChildrenByMotherRepository;
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
 * Integration tests for the {@link ChildrenByMotherResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class ChildrenByMotherResourceIT {

    private static final String DEFAULT_MOTHER_ID = "AAAAAAAAAA";
    private static final String UPDATED_MOTHER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_CHILD_ID = "AAAAAAAAAA";
    private static final String UPDATED_CHILD_ID = "BBBBBBBBBB";

    private static final String DEFAULT_FATHER_ID = "AAAAAAAAAA";
    private static final String UPDATED_FATHER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final Gender DEFAULT_GENDER = Gender.MALE;
    private static final Gender UPDATED_GENDER = Gender.FEMALE;

    private static final Instant DEFAULT_DOB = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DOB = Instant.ofEpochMilli(1703886261863L);

    private static final String ENTITY_API_URL = "/api/children-by-mothers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ChildrenByMotherRepository childrenByMotherRepository;

    @Autowired
    private R2dbcEntityTemplate template;

    @Autowired
    private WebTestClient webTestClient;

    private ChildrenByMother childrenByMother;

    private ChildrenByMother insertedChildrenByMother;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChildrenByMother createEntity() {
        return new ChildrenByMother()
            .motherId(DEFAULT_MOTHER_ID)
            .childId(DEFAULT_CHILD_ID)
            .fatherId(DEFAULT_FATHER_ID)
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
    public static ChildrenByMother createUpdatedEntity() {
        return new ChildrenByMother()
            .motherId(UPDATED_MOTHER_ID)
            .childId(UPDATED_CHILD_ID)
            .fatherId(UPDATED_FATHER_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB);
    }

    public static void deleteEntities(R2dbcEntityTemplate template) {
        try {
            template.delete(ChildrenByMother.class).all().block();
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
        childrenByMother = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedChildrenByMother != null) {
            childrenByMotherRepository.delete(insertedChildrenByMother).block();
            insertedChildrenByMother = null;
        }
        deleteEntities(template);
    }

    @Test
    void createChildrenByMother() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ChildrenByMother
        var returnedChildrenByMother = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ChildrenByMother.class)
            .returnResult()
            .getResponseBody();

        // Validate the ChildrenByMother in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertChildrenByMotherUpdatableFieldsEquals(returnedChildrenByMother, getPersistedChildrenByMother(returnedChildrenByMother));

        insertedChildrenByMother = returnedChildrenByMother;
    }

    @Test
    void createChildrenByMotherWithExistingId() throws Exception {
        // Create the ChildrenByMother with an existing ID
        childrenByMother.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByMother in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkMotherIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        childrenByMother.setMotherId(null);

        // Create the ChildrenByMother, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkChildIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        childrenByMother.setChildId(null);

        // Create the ChildrenByMother, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        childrenByMother.setFirstName(null);

        // Create the ChildrenByMother, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkLastNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        childrenByMother.setLastName(null);

        // Create the ChildrenByMother, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllChildrenByMothers() {
        // Initialize the database
        insertedChildrenByMother = childrenByMotherRepository.save(childrenByMother).block();

        // Get all the childrenByMotherList
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
            .value(hasItem(childrenByMother.getId().intValue()))
            .jsonPath("$.[*].motherId")
            .value(hasItem(DEFAULT_MOTHER_ID))
            .jsonPath("$.[*].childId")
            .value(hasItem(DEFAULT_CHILD_ID))
            .jsonPath("$.[*].fatherId")
            .value(hasItem(DEFAULT_FATHER_ID))
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
    void getChildrenByMother() {
        // Initialize the database
        insertedChildrenByMother = childrenByMotherRepository.save(childrenByMother).block();

        // Get the childrenByMother
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, childrenByMother.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(childrenByMother.getId().intValue()))
            .jsonPath("$.motherId")
            .value(is(DEFAULT_MOTHER_ID))
            .jsonPath("$.childId")
            .value(is(DEFAULT_CHILD_ID))
            .jsonPath("$.fatherId")
            .value(is(DEFAULT_FATHER_ID))
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
    void getNonExistingChildrenByMother() {
        // Get the childrenByMother
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingChildrenByMother() throws Exception {
        // Initialize the database
        insertedChildrenByMother = childrenByMotherRepository.save(childrenByMother).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the childrenByMother
        ChildrenByMother updatedChildrenByMother = childrenByMotherRepository.findById(childrenByMother.getId()).block();
        updatedChildrenByMother
            .motherId(UPDATED_MOTHER_ID)
            .childId(UPDATED_CHILD_ID)
            .fatherId(UPDATED_FATHER_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedChildrenByMother.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedChildrenByMother))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChildrenByMother in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedChildrenByMotherToMatchAllProperties(updatedChildrenByMother);
    }

    @Test
    void putNonExistingChildrenByMother() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByMother.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, childrenByMother.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByMother in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchChildrenByMother() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByMother.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByMother in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamChildrenByMother() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByMother.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ChildrenByMother in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateChildrenByMotherWithPatch() throws Exception {
        // Initialize the database
        insertedChildrenByMother = childrenByMotherRepository.save(childrenByMother).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the childrenByMother using partial update
        ChildrenByMother partialUpdatedChildrenByMother = new ChildrenByMother();
        partialUpdatedChildrenByMother.setId(childrenByMother.getId());

        partialUpdatedChildrenByMother.childId(UPDATED_CHILD_ID).firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedChildrenByMother.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedChildrenByMother))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChildrenByMother in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChildrenByMotherUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedChildrenByMother, childrenByMother),
            getPersistedChildrenByMother(childrenByMother)
        );
    }

    @Test
    void fullUpdateChildrenByMotherWithPatch() throws Exception {
        // Initialize the database
        insertedChildrenByMother = childrenByMotherRepository.save(childrenByMother).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the childrenByMother using partial update
        ChildrenByMother partialUpdatedChildrenByMother = new ChildrenByMother();
        partialUpdatedChildrenByMother.setId(childrenByMother.getId());

        partialUpdatedChildrenByMother
            .motherId(UPDATED_MOTHER_ID)
            .childId(UPDATED_CHILD_ID)
            .fatherId(UPDATED_FATHER_ID)
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedChildrenByMother.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedChildrenByMother))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChildrenByMother in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChildrenByMotherUpdatableFieldsEquals(
            partialUpdatedChildrenByMother,
            getPersistedChildrenByMother(partialUpdatedChildrenByMother)
        );
    }

    @Test
    void patchNonExistingChildrenByMother() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByMother.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, childrenByMother.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByMother in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchChildrenByMother() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByMother.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChildrenByMother in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamChildrenByMother() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        childrenByMother.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(childrenByMother))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ChildrenByMother in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteChildrenByMother() {
        // Initialize the database
        insertedChildrenByMother = childrenByMotherRepository.save(childrenByMother).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the childrenByMother
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, childrenByMother.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return childrenByMotherRepository.count().block();
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

    protected ChildrenByMother getPersistedChildrenByMother(ChildrenByMother childrenByMother) {
        return childrenByMotherRepository.findById(childrenByMother.getId()).block();
    }

    protected void assertPersistedChildrenByMotherToMatchAllProperties(ChildrenByMother expectedChildrenByMother) {
        // Test fails because reactive api returns an empty object instead of null
        // assertChildrenByMotherAllPropertiesEquals(expectedChildrenByMother, getPersistedChildrenByMother(expectedChildrenByMother));
        assertChildrenByMotherUpdatableFieldsEquals(expectedChildrenByMother, getPersistedChildrenByMother(expectedChildrenByMother));
    }

    protected void assertPersistedChildrenByMotherToMatchUpdatableProperties(ChildrenByMother expectedChildrenByMother) {
        // Test fails because reactive api returns an empty object instead of null
        // assertChildrenByMotherAllUpdatablePropertiesEquals(expectedChildrenByMother, getPersistedChildrenByMother(expectedChildrenByMother));
        assertChildrenByMotherUpdatableFieldsEquals(expectedChildrenByMother, getPersistedChildrenByMother(expectedChildrenByMother));
    }
}
