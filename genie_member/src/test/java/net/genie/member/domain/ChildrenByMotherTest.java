package net.genie.member.domain;

import static net.genie.member.domain.ChildrenByMotherTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.genie.member.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ChildrenByMotherTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ChildrenByMother.class);
        ChildrenByMother childrenByMother1 = getChildrenByMotherSample1();
        ChildrenByMother childrenByMother2 = new ChildrenByMother();
        assertThat(childrenByMother1).isNotEqualTo(childrenByMother2);

        childrenByMother2.setId(childrenByMother1.getId());
        assertThat(childrenByMother1).isEqualTo(childrenByMother2);

        childrenByMother2 = getChildrenByMotherSample2();
        assertThat(childrenByMother1).isNotEqualTo(childrenByMother2);
    }
}
