package net.genie.member.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ChildrenByMotherTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static ChildrenByMother getChildrenByMotherSample1() {
        return new ChildrenByMother()
            .id(1L)
            .motherId("motherId1")
            .childId("childId1")
            .fatherId("fatherId1")
            .firstName("firstName1")
            .lastName("lastName1");
    }

    public static ChildrenByMother getChildrenByMotherSample2() {
        return new ChildrenByMother()
            .id(2L)
            .motherId("motherId2")
            .childId("childId2")
            .fatherId("fatherId2")
            .firstName("firstName2")
            .lastName("lastName2");
    }

    public static ChildrenByMother getChildrenByMotherRandomSampleGenerator() {
        return new ChildrenByMother()
            .id(longCount.incrementAndGet())
            .motherId(UUID.randomUUID().toString())
            .childId(UUID.randomUUID().toString())
            .fatherId(UUID.randomUUID().toString())
            .firstName(UUID.randomUUID().toString())
            .lastName(UUID.randomUUID().toString());
    }
}
