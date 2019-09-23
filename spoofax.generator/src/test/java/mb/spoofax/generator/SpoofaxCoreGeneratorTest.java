package mb.spoofax.generator;

import org.junit.jupiter.api.Test;

public class SpoofaxCoreGeneratorTest {
    @Test
    public void test() {
        final ImmutableSpoofaxCoreGeneratorInput input = ImmutableSpoofaxCoreGeneratorInput.builder()
            .groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tiger")
            .build();
    }
}
