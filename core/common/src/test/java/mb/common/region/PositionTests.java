package mb.common.region;

import mb.common.editing.TextEdit;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the {@link Position} class. */
@SuppressWarnings("CodeBlock2Expr")
@DisplayName("Position")
public class PositionTests {

    /** Tests the {@link Position#fromLineChar(int, int)} function. */
    @DisplayName("fromLineChar(int, int)")
    @Nested public class FromLineCharTests {

        @Test
        @DisplayName("returns position with specified line and character offsets")
        public void returnsPositionWithSpecifiedLineAndCharacterOffsets() {
            // Arrange
            int line = 3;
            int character = 7;

            // Act
            Position sut = Position.fromLineChar(line, character);

            // Assert
            assertEquals(line, sut.getLine());
            assertEquals(character, sut.getCharacter());
        }

        @Test
        @DisplayName("when line number is zero, throws exception")
        public void whenLineNumberIsZero_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Position.fromLineChar(0, 7);
            });
        }

        @Test
        @DisplayName("when line number is negative, throws exception")
        public void whenLineNumberIsNegative_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Position.fromLineChar(-3, 7);
            });
        }

        @Test
        @DisplayName("when character offset is zero, throws exception")
        public void whenCharacterOffsetIsZero_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Position.fromLineChar(3, 0);
            });
        }

        @Test
        @DisplayName("when character offset is negative, throws exception")
        public void whenCharacterOffsetNumberIsNegative_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Position.fromLineChar(3, -7);
            });
        }

    }


    /** Tests the {@link Position#fromString(String)} function. */
    @DisplayName("fromString(String)")
    @Nested public class FromStringTests {

        @Test
        @DisplayName("when string is 'line:char', parses successfully")
        public void whenStringIsLineChar_parsesSuccessfully() {
            // Act
            @Nullable Position sut = Position.fromString("3:2");

            // Assert
            assertNotNull(sut);
            assertEquals(3, sut.getLine());
            assertEquals(2, sut.getCharacter());
        }

        @Test
        @DisplayName("when line is zero, returns null")
        public void whenLineIsZero_returnsNull() {
            // Act
            @Nullable Position sut = Position.fromString("0:2");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when line is negative, returns null")
        public void whenLineIsNegative_returnsNull() {
            // Act
            @Nullable Position sut = Position.fromString("-1:2");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when character offset is zero, returns null")
        public void whenCharacterOffsetIsZero_returnsNull() {
            // Act
            @Nullable Position sut = Position.fromString("3:0");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when character offset is negative, returns null")
        public void whenCharacterOffsetIsNegative_returnsNull() {
            // Act
            @Nullable Position sut = Position.fromString("3:-1");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when format is invalid (1), returns null")
        public void whenFormatIsInvalid1_returnsNull() {
            // Act
            @Nullable Position sut = Position.fromString(":3");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when format is invalid (2), returns null")
        public void whenFormatIsInvalid2_returnsNull() {
            // Act
            @Nullable Position sut = Position.fromString(":");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when format is invalid (3), returns null")
        public void whenFormatIsInvalid3_returnsNull() {
            // Act
            @Nullable Position sut = Position.fromString("3:");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when numbers cannot be parsed, returns null")
        public void whenNumbersCannotBeParsed_returnsNull() {
            // Act
            @Nullable Position sut = Position.fromString("aa:bb");

            // Assert
            assertNull(sut);
        }

    }

    /** Tests the {@link Position#getLine()} property. */
    @DisplayName("getLine()")
    @Nested public class GetLineTests {

        @Test
        @DisplayName("returns line number")
        public void returnsLineNumber() {
            // Arrange
            Position sut = Position.fromLineChar(3, 7);

            // Assert
            assertEquals(3, sut.getLine());
        }

    }

    /** Tests the {@link Position#getCharacter()} property. */
    @DisplayName("getCharacter()")
    @Nested public class GetCharacterTests {

        @Test
        @DisplayName("returns character offset")
        public void returnsLineNumber() {
            // Arrange
            Position sut = Position.fromLineChar(3, 7);

            // Assert
            assertEquals(3, sut.getLine());
        }

    }

    /** Tests the contract of the {@link Position#equals(Object)} and {@link Position#hashCode()} methods. */
    @Test
    public void equalityContract() {
        EqualsVerifier.forClass(Position.class).verify();
    }

    /** Tests the {@link Position#toString()} method. */
    @DisplayName("toString()")
    @Nested public class ToStringTests {

        @Test
        @DisplayName("returns 'line:character' string")
        public void returnsLineCharacterString() {
            // Arrange
            Position sut = Position.fromLineChar(3, 7);

            // Act
            String result = sut.toString();

            // Assert
            assertEquals("3:7", result);
        }

    }

}
