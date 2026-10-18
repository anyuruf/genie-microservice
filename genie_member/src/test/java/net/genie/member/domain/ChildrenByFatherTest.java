package net.genie.member.domain;

import static net.genie.member.domain.ChildrenByFatherTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.genie.member.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ChildrenByFatherTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ChildrenByFather.class);
        ChildrenByFather childrenByFather1 = getChildrenByFatherSample1();
        ChildrenByFather childrenByFather2 = new ChildrenByFather();
        assertThat(childrenByFather1).isNotEqualTo(childrenByFather2);

        childrenByFather2.setId(childrenByFather1.getId());
        assertThat(childrenByFather1).isEqualTo(childrenByFather2);

        childrenByFather2 = getChildrenByFatherSample2();
        assertThat(childrenByFather1).isNotEqualTo(childrenByFather2);
    }
}
