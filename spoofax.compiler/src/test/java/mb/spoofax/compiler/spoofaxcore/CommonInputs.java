package mb.spoofax.compiler.spoofaxcore;

public class CommonInputs {
    public static Coordinates.Builder tigerCoordinatesBuilder() {
        return Coordinates.builder().groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tiger");
    }

    public static Coordinates tigerCoordinates() {
        return tigerCoordinatesBuilder().build();
    }
}
