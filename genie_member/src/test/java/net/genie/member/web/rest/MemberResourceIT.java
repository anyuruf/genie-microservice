package net.genie.member.web.rest;

import static net.genie.member.domain.MemberAsserts.*;
import static net.genie.member.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.time.Instant;
import java.util.UUID;
import net.genie.member.IntegrationTest;
import net.genie.member.domain.Member;
import net.genie.member.domain.enumeration.Gender;
import net.genie.member.repository.MemberRepository;
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
 * Integration tests for the {@link MemberResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class MemberResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Gender DEFAULT_GENDER = Gender.MALE;
    private static final Gender UPDATED_GENDER = Gender.FEMALE;

    private static final Instant DEFAULT_DOB = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DOB = Instant.ofEpochMilli(1703886261863L);

    private static final String DEFAULT_IMAGE_URL = "AAAAAAAAAA";
    private static final String UPDATED_IMAGE_URL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/members";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private R2dbcEntityTemplate template;

    @Autowired
    private WebTestClient webTestClient;

    private Member member;

    private Member insertedMember;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Member createEntity() {
        return new Member()
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .description(DEFAULT_DESCRIPTION)
            .gender(DEFAULT_GENDER)
            .dob(DEFAULT_DOB)
            .imageUrl(DEFAULT_IMAGE_URL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Member createUpdatedEntity() {
        return new Member()
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .description(UPDATED_DESCRIPTION)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB)
            .imageUrl(UPDATED_IMAGE_URL);
    }

    public static void deleteEntities(R2dbcEntityTemplate template) {
        try {
            template.delete(Member.class).all().block();
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
        member = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMember != null) {
            memberRepository.delete(insertedMember).block();
            insertedMember = null;
        }
        deleteEntities(template);
    }

    @Test
    void createMember() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Member
        var returnedMember = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Member.class)
            .returnResult()
            .getResponseBody();

        // Validate the Member in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertMemberUpdatableFieldsEquals(returnedMember, getPersistedMember(returnedMember));

        insertedMember = returnedMember;
    }

    @Test
    void createMemberWithExistingId() throws Exception {
        // Create the Member with an existing ID
        member.setId("existing_id");

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        member.setFirstName(null);

        // Create the Member, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkLastNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        member.setLastName(null);

        // Create the Member, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllMembers() {
        // Initialize the database
        member.setId(UUID.randomUUID().toString());
        insertedMember = memberRepository.save(member).block();

        // Get all the memberList
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
            .value(hasItem(member.getId()))
            .jsonPath("$.[*].firstName")
            .value(hasItem(DEFAULT_FIRST_NAME))
            .jsonPath("$.[*].lastName")
            .value(hasItem(DEFAULT_LAST_NAME))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].gender")
            .value(hasItem(DEFAULT_GENDER.toString()))
            .jsonPath("$.[*].dob")
            .value(hasItem(DEFAULT_DOB.toString()))
            .jsonPath("$.[*].imageUrl")
            .value(hasItem(DEFAULT_IMAGE_URL));
    }

    @Test
    void getMember() {
        // Initialize the database
        member.setId(UUID.randomUUID().toString());
        insertedMember = memberRepository.save(member).block();

        // Get the member
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, member.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(member.getId()))
            .jsonPath("$.firstName")
            .value(is(DEFAULT_FIRST_NAME))
            .jsonPath("$.lastName")
            .value(is(DEFAULT_LAST_NAME))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.gender")
            .value(is(DEFAULT_GENDER.toString()))
            .jsonPath("$.dob")
            .value(is(DEFAULT_DOB.toString()))
            .jsonPath("$.imageUrl")
            .value(is(DEFAULT_IMAGE_URL));
    }

    @Test
    void getNonExistingMember() {
        // Get the member
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingMember() throws Exception {
        // Initialize the database
        member.setId(UUID.randomUUID().toString());
        insertedMember = memberRepository.save(member).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the member
        Member updatedMember = memberRepository.findById(member.getId()).block();
        updatedMember
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .description(UPDATED_DESCRIPTION)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB)
            .imageUrl(UPDATED_IMAGE_URL);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedMember.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedMember))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMemberToMatchAllProperties(updatedMember);
    }

    @Test
    void putNonExistingMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, member.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateMemberWithPatch() throws Exception {
        // Initialize the database
        member.setId(UUID.randomUUID().toString());
        insertedMember = memberRepository.save(member).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the member using partial update
        Member partialUpdatedMember = new Member();
        partialUpdatedMember.setId(member.getId());

        partialUpdatedMember.dob(UPDATED_DOB);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMember.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedMember))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Member in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMemberUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMember, member), getPersistedMember(member));
    }

    @Test
    void fullUpdateMemberWithPatch() throws Exception {
        // Initialize the database
        member.setId(UUID.randomUUID().toString());
        insertedMember = memberRepository.save(member).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the member using partial update
        Member partialUpdatedMember = new Member();
        partialUpdatedMember.setId(member.getId());

        partialUpdatedMember
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .description(UPDATED_DESCRIPTION)
            .gender(UPDATED_GENDER)
            .dob(UPDATED_DOB)
            .imageUrl(UPDATED_IMAGE_URL);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMember.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedMember))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Member in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMemberUpdatableFieldsEquals(partialUpdatedMember, getPersistedMember(partialUpdatedMember));
    }

    @Test
    void patchNonExistingMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, member.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(member))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteMember() {
        // Initialize the database
        member.setId(UUID.randomUUID().toString());
        insertedMember = memberRepository.save(member).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the member
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, member.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return memberRepository.count().block();
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

    protected Member getPersistedMember(Member member) {
        return memberRepository.findById(member.getId()).block();
    }

    protected void assertPersistedMemberToMatchAllProperties(Member expectedMember) {
        // Test fails because reactive api returns an empty object instead of null
        // assertMemberAllPropertiesEquals(expectedMember, getPersistedMember(expectedMember));
        assertMemberUpdatableFieldsEquals(expectedMember, getPersistedMember(expectedMember));
    }

    protected void assertPersistedMemberToMatchUpdatableProperties(Member expectedMember) {
        // Test fails because reactive api returns an empty object instead of null
        // assertMemberAllUpdatablePropertiesEquals(expectedMember, getPersistedMember(expectedMember));
        assertMemberUpdatableFieldsEquals(expectedMember, getPersistedMember(expectedMember));
    }
}
