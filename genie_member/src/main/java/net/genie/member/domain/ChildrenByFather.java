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
 * A ChildrenByFather.
 */
@Table("children_by_father")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChildrenByFather implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Column("father_id")
    private String fatherId;

    @NotNull
    @Column("child_id")
    private String childId;

    @Column("mother_id")
    private String motherId;

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

    public ChildrenByFather id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFatherId() {
        return this.fatherId;
    }

    public ChildrenByFather fatherId(String fatherId) {
        this.setFatherId(fatherId);
        return this;
    }

    public void setFatherId(String fatherId) {
        this.fatherId = fatherId;
    }

    public String getChildId() {
        return this.childId;
    }

    public ChildrenByFather childId(String childId) {
        this.setChildId(childId);
        return this;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getMotherId() {
        return this.motherId;
    }

    public ChildrenByFather motherId(String motherId) {
        this.setMotherId(motherId);
        return this;
    }

    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public ChildrenByFather firstName(String firstName) {
        this.setFirstName(firstName);
        return this;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public ChildrenByFather lastName(String lastName) {
        this.setLastName(lastName);
        return this;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return this.gender;
    }

    public ChildrenByFather gender(Gender gender) {
        this.setGender(gender);
        return this;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Instant getDob() {
        return this.dob;
    }

    public ChildrenByFather dob(Instant dob) {
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
        if (!(o instanceof ChildrenByFather)) {
            return false;
        }
        return getId() != null && getId().equals(((ChildrenByFather) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChildrenByFather{" +
            "id=" + getId() +
            ", fatherId='" + getFatherId() + "'" +
            ", childId='" + getChildId() + "'" +
            ", motherId='" + getMotherId() + "'" +
            ", firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", gender='" + getGender() + "'" +
            ", dob='" + getDob() + "'" +
            "}";
    }
}
