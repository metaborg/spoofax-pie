package mb.common.editing;

import mb.common.region.Position;
import mb.common.region.Region;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests the {@link TextEdit} class. */
@SuppressWarnings("CodeBlock2Expr")
@DisplayName("TextEdit")
public class TextEditTests {

    /** Tests the {@link TextEdit#TextEdit(Region, String)} constructor. */
    @DisplayName("TextEdit(Region, String)")
    @Nested public class ConstructorTests {

        @Test
        @DisplayName("returns instance with specified arguments")
        public void returnsInstanceWithSpecifiedArguments() {
            // Arrange
            Region region = Region.fromOffsets(2, 4);
            String newText = "new";

            // Act
            TextEdit sut = new TextEdit(region, newText);

            // Assert
            assertEquals(region, sut.getRegion());
            assertEquals(newText, sut.getNewText());
        }

    }

    /** Tests the {@link TextEdit#getRegion()} property. */
    @DisplayName("getRegion()")
    @Nested public class GetRegionTests {

        @Test
        @DisplayName("returns the region")
        public void returnsInstanceWithSpecifiedArguments() {
            // Arrange
            Region region = Region.fromOffsets(2, 4);
            String newText = "new";
            TextEdit sut = new TextEdit(region, newText);

            // Assert
            assertEquals(region, sut.getRegion());
        }

    }

    /** Tests the {@link TextEdit#getNewText()} property. */
    @DisplayName("getNewText()")
    @Nested public class GetNewTextTests {

        @Test
        @DisplayName("returns the new text")
        public void returnsInstanceWithSpecifiedArguments() {
            // Arrange
            Region region = Region.fromOffsets(2, 4);
            String newText = "new";
            TextEdit sut = new TextEdit(region, newText);

            // Assert
            assertEquals(newText, sut.getNewText());
        }

    }

    /** Tests the contract of the {@link TextEdit#equals(Object)} and {@link TextEdit#hashCode()} methods. */
    @Test
    public void equalityContract() {
        EqualsVerifier.forClass(TextEdit.class)
            // Because EqualsVerifier does not understand default @NonNull yet:
            .withNonnullFields("region", "newText")
            .verify();
    }

}
