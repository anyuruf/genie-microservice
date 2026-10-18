package net.genie.member.domain;

import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import net.genie.member.domain.enumeration.Gender;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A ChildrenByMother.
 */
@Table("children_by_mother")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChildrenByMother implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Column("mother_id")
    private String motherId;

    @NotNull
    @Column("child_id")
    private String childId;

    @Column("father_id")
    private String fatherId;

    @NotNull
    @Column("first_name")
    private String firstName;

    @NotNull
    @Column("last_name")
    private String lastName;

    @Column("gender")
    private Gender gender;

    @Column("dob")
    private Instant dob;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ChildrenByMother id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMotherId() {
        return this.motherId;
    }

    public ChildrenByMother motherId(String motherId) {
        this.setMotherId(motherId);
        return this;
    }

    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }

    public String getChildId() {
        return this.childId;
    }

    public ChildrenByMother childId(String childId) {
        this.setChildId(childId);
        return this;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getFatherId() {
        return this.fatherId;
    }

    public ChildrenByMother fatherId(String fatherId) {
        this.setFatherId(fatherId);
        return this;
    }

    public void setFatherId(String fatherId) {
        this.fatherId = fatherId;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public ChildrenByMother firstName(String firstName) {
        this.setFirstName(firstName);
        return this;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public ChildrenByMother lastName(String lastName) {
        this.setLastName(lastName);
        return this;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return this.gender;
    }

    public ChildrenByMother gender(Gender gender) {
        this.setGender(gender);
        return this;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Instant getDob() {
        return this.dob;
    }

    public ChildrenByMother dob(Instant dob) {
        this.setDob(dob);
        return this;
    }

    public void setDob(Instant dob) {
        this.dob = dob;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChildrenByMother)) {
            return false;
        }
        return getId() != null && getId().equals(((ChildrenByMother) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChildrenByMother{" +
            "id=" + getId() +
            ", motherId='" + getMotherId() + "'" +
            ", childId='" + getChildId() + "'" +
            ", fatherId='" + getFatherId() + "'" +
            ", firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", gender='" + getGender() + "'" +
            ", dob='" + getDob() + "'" +
            "}";
    }
}
