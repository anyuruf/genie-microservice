package net.genie.member.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ChildrenByFatherTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static ChildrenByFather getChildrenByFatherSample1() {
        return new ChildrenByFather()
            .id(1L)
            .fatherId("fatherId1")
            .childId("childId1")
            .motherId("motherId1")
            .firstName("firstName1")
            .lastName("lastName1");
    }

    public static ChildrenByFather getChildrenByFatherSample2() {
        return new ChildrenByFather()
            .id(2L)
            .fatherId("fatherId2")
            .childId("childId2")
            .motherId("motherId2")
            .firstName("firstName2")
            .lastName("lastName2");
    }

    public static ChildrenByFather getChildrenByFatherRandomSampleGenerator() {
        return new ChildrenByFather()
            .id(longCount.incrementAndGet())
            .fatherId(UUID.randomUUID().toString())
            .childId(UUID.randomUUID().toString())
            .motherId(UUID.randomUUID().toString())
            .firstName(UUID.randomUUID().toString())
            .lastName(UUID.randomUUID().toString());
    }
}
