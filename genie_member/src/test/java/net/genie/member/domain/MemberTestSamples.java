package net.genie.member.domain;

import java.util.UUID;

public class MemberTestSamples {

    public static Member getMemberSample1() {
        return new Member().id("id1").firstName("firstName1").lastName("lastName1").description("description1").imageUrl("imageUrl1");
    }

    public static Member getMemberSample2() {
        return new Member().id("id2").firstName("firstName2").lastName("lastName2").description("description2").imageUrl("imageUrl2");
    }

    public static Member getMemberRandomSampleGenerator() {
        return new Member()
            .id(UUID.randomUUID().toString())
            .firstName(UUID.randomUUID().toString())
            .lastName(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .imageUrl(UUID.randomUUID().toString());
    }
}
