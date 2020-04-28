package mb.common.style;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the {@link StyleName} class. */
@SuppressWarnings("CodeBlock2Expr")
@DisplayName("StyleName")
public class StyleNameTests {

    /** Tests the {@link StyleName#defaultStyleName()} function. */
    @DisplayName("defaultStyleName()")
    @Nested public class DefaultStyleNameTests {

        @Test
        public void returnsDefaultStyleName() {
            // Act
            StyleName sut = StyleName.defaultStyleName();

            // Assert
            assertTrue(sut.isDefault());
            assertEquals("<default>", sut.toString());
        }

    }

    private static Stream<Arguments> provideValidityTestData() {
        return Stream.of(
            // @formatter:off
            Arguments.of("a", true),
            Arguments.of("abc", true),
            Arguments.of("A", true),
            Arguments.of("ABC", true),
            Arguments.of("a123", true),
            Arguments.of("A123", true),
            Arguments.of("_xyz", true),
            Arguments.of("x_y_z", true),
            Arguments.of("x-y-z", true),
            Arguments.of("x-123-z", true),
            Arguments.of("___xyz", true),

            Arguments.of("", false),
            Arguments.of("---", false),
            Arguments.of("123", false),
            Arguments.of("123a", false),
            Arguments.of("-xyz", false),
            Arguments.of("-xyz", false)
            // @formatter:on
        );
    }

    /** Tests the {@link StyleName#isValidPartName(String)} function. */
    @DisplayName("isValidPartName()")
    @Nested public class IsValidPartNameTests {

        @ParameterizedTest(name = "part name {0} valid? {1}")
        @MethodSource("mb.common.style.StyleNameTests#provideValidityTestData")
        public void partNameIsValid(String partName, boolean expectValid) {
            // Act
            boolean isValid = StyleName.isValidPartName(partName);

            // Assert
            assertEquals(expectValid, isValid);
        }

    }

    /** Tests the {@link StyleName#of(String...)} function. */
    @DisplayName("of(String...)")
    @Nested public class OfArrayTests {

        @Test
        @DisplayName("returns StyleName of parts")
        public void returnsStyleNameOfParts() {
            // Act
            StyleName sut = StyleName.of("abc", "def", "ghi");

            // Assert
            assertEquals("abc.def.ghi", sut.toString());
        }

        @Test
        @DisplayName("returns StyleName of a single part")
        public void returnsStyleNameOfASinglePart() {
            // Act
            StyleName sut = StyleName.of("abc");

            // Assert
            assertEquals("abc", sut.toString());
        }

        @Test
        @DisplayName("returns StyleName of an empty array")
        public void returnsStyleNameOfAnEmptyArray() {
            // Act
            StyleName sut = StyleName.of();

            // Assert
            assertEquals("<default>", sut.toString());
        }

        @Test
        @DisplayName("when one of the part names is invalid, throws exception")
        public void whenOneOfThePartNamesIsInvalid_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                StyleName.of("abc", "---", "def");
            });
        }

        @Test
        @DisplayName("when one of the part names is empty, throws exception")
        public void whenOneOfThePartNamesIsEmpty_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                StyleName.of("abc", "", "def");
            });
        }

    }


    /** Tests the {@link StyleName#of(Iterable)} function. */
    @DisplayName("of(Iterable)")
    @Nested public class OfIterableTests {

        @Test
        @DisplayName("returns StyleName of parts")
        public void returnsStyleNameOfParts() {
            // Act
            StyleName sut = StyleName.of(Arrays.asList("abc", "def", "ghi"));

            // Assert
            assertEquals("abc.def.ghi", sut.toString());
        }

        @Test
        @DisplayName("returns StyleName of a single part")
        public void returnsStyleNameOfASinglePart() {
            // Act
            StyleName sut = StyleName.of(Collections.singletonList("abc"));

            // Assert
            assertEquals("abc", sut.toString());
        }

        @Test
        @DisplayName("returns StyleName of an empty array")
        public void returnsStyleNameOfAnEmptyArray() {
            // Act
            StyleName sut = StyleName.of(Collections.emptyList());

            // Assert
            assertEquals("<default>", sut.toString());
        }

        @Test
        @DisplayName("when one of the part names is invalid, throws exception")
        public void whenOneOfThePartNamesIsInvalid_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                StyleName.of(Arrays.asList("abc", "---", "def"));
            });
        }

        @Test
        @DisplayName("when one of the part names is empty, throws exception")
        public void whenOneOfThePartNamesIsEmpty_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                StyleName.of(Arrays.asList("abc", "", "def"));
            });
        }

    }

    /** Tests the {@link StyleName#fromString(String)} function. */
    @DisplayName("fromString(String)")
    @Nested public class FromStringTests {

        @Test
        @DisplayName("returns StyleName parsed from multiple parts")
        public void returnsStyleNameParsedFromMultipleParts() {
            // Act
            @Nullable StyleName sut = StyleName.fromString("abc.def.ghi");

            // Assert
            assertNotNull(sut);
            assertEquals("abc.def.ghi", sut.toString());
        }

        @Test
        @DisplayName("returns StyleName parsed from a single part")
        public void returnsStyleNameParsedFromASinglePart() {
            // Act
            @Nullable StyleName sut = StyleName.fromString("abc");

            // Assert
            assertNotNull(sut);
            assertEquals("abc", sut.toString());
        }

        @Test
        @DisplayName("returns StyleName parsed from an empty string")
        public void returnsStyleNameParsedFromEmptyString() {
            // Act
            @Nullable StyleName sut = StyleName.fromString("");

            // Assert
            assertNotNull(sut);
            assertEquals("<default>", sut.toString());
        }

        @Test
        @DisplayName("returns StyleName parsed from default string")
        public void returnsStyleNameParsedFromDefaultString() {
            // Act
            @Nullable StyleName sut = StyleName.fromString("<default>");

            // Assert
            assertNotNull(sut);
            assertEquals("<default>", sut.toString());
        }

        @Test
        @DisplayName("when one of the part names is invalid, returns null")
        public void whenOneOfThePartNamesIsInvalid_returnsNull() {
            // Act
            @Nullable StyleName sut = StyleName.fromString("abc.---.def");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when one of the part names is empty, returns null")
        public void whenOneOfThePartNamesIsEmpty_returnsNull() {
            // Act
            @Nullable StyleName sut = StyleName.fromString("abc..def");

            // Assert
            assertNull(sut);
        }

    }


    /** Tests the {@link StyleName#isDefault()} property. */
    @DisplayName("isDefault()")
    @Nested public class IsDefaultTests {

        @Test
        @DisplayName("when default style name, returns true")
        public void whenDefaultStyleName_returnsTrue() {
            // Arrange
            StyleName sut = StyleName.defaultStyleName();

            // Assert
            assertTrue(sut.isDefault());
        }


        @Test
        @DisplayName("when non-default style name, returns false")
        public void whenNonDefaultStyleName_returnsTrue() {
            // Arrange
            StyleName sut = StyleName.of("abc", "def");

            // Assert
            assertFalse(sut.isDefault());
        }

    }

    /** Tests the {@link StyleName#startsWith(StyleName)} property. */
    @DisplayName("startsWith(StyleName)")
    @Nested public class StartsWithStyleNameTests {

        @Test
        @DisplayName("when both default, returns true")
        public void whenBothDefault_returnsTrue() {
            // Arrange
            StyleName a = StyleName.defaultStyleName();
            StyleName b = Objects.requireNonNull(StyleName.fromString(""));

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when equal, returns true")
        public void whenEqual_returnsTrue() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            StyleName b = Objects.requireNonNull(StyleName.fromString("abc.def"));

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when second is default, returns true")
        public void whenSecondIsDefault_returnsTrue() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            StyleName b = StyleName.defaultStyleName();

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when second is prefix, returns true")
        public void whenSecondIsPrefix_returnsTrue() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            StyleName b = StyleName.of("abc");

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when second is string prefix but not name prefix, returns false")
        public void whenSecondIsStringPrefixButNotNamePrefix_returnsFalse() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            StyleName b = StyleName.of("abc", "de");

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when second is different (1), returns false")
        public void whenSecondIsDifferent1_returnsFalse() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            StyleName b = StyleName.of("abc", "DEF");

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when second is different (2), returns false")
        public void whenSecondIsDifferent2_returnsFalse() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            StyleName b = StyleName.of("ABC", "XYZ");

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when first is default, returns false")
        public void whenFirstIsDefault_returnsFalse() {
            // Arrange
            StyleName a = StyleName.defaultStyleName();
            StyleName b = StyleName.of("abc", "def");

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when second is longer, returns false")
        public void whenSecondIsLonger_returnsFalse() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            StyleName b = StyleName.of("abc", "def", "ghi");

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

    }


    /** Tests the {@link StyleName#startsWith(String)} property. */
    @DisplayName("startsWith(String)")
    @Nested public class StartsWithStringTests {

        @Test
        @DisplayName("when both default, returns true")
        public void whenBothDefault_returnsTrue() {
            // Arrange
            StyleName a = StyleName.defaultStyleName();
            String b = "";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when equal, returns true")
        public void whenEqual_returnsTrue() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            String b = "abc.def";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when second is default, returns true")
        public void whenSecondIsDefault_returnsTrue() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            String b = "<default>";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when second is prefix, returns true")
        public void whenSecondIsPrefix_returnsTrue() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            String b = "abc";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when second is string prefix but not name prefix, returns false")
        public void whenSecondIsStringPrefixButNotNamePrefix_returnsFalse() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            String b = "abc.de";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when second is different (1), returns false")
        public void whenSecondIsDifferent1_returnsFalse() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            String b = "abc.DEF";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when second is different (2), returns false")
        public void whenSecondIsDifferent2_returnsFalse() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            String b = "ABC.XYZ";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when first is default, returns false")
        public void whenFirstIsDefault_returnsFalse() {
            // Arrange
            StyleName a = StyleName.defaultStyleName();
            String b = "abc.def";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when second is longer, returns false")
        public void whenSecondIsLonger_returnsFalse() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");
            String b = "abc.def.ghi";

            // Act
            boolean result = a.startsWith(b);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when second is invalid, throws exception")
        public void whenSecondIsInvalid_throwsException() {
            // Arrange
            StyleName a = StyleName.of("abc", "def");

            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                a.startsWith("---");
            });
        }

    }

    /** Tests the contract of the {@link StyleName#equals(Object)} and {@link StyleName#hashCode()} methods. */
    @Test
    public void equalityContract() {
        EqualsVerifier.forClass(StyleName.class).verify();
    }


    /** Tests the {@link StyleName#toString()} method. */
    @DisplayName("toString(String)")
    @Nested public class ToStringTests {

        @Test
        @DisplayName("returns dotted names")
        public void returnsDottedNames() {
            // Act
            StyleName sut = StyleName.of("abc", "def", "ghi");

            // Assert
            assertEquals("abc.def.ghi", sut.toString());
        }

        @Test
        @DisplayName("returns singleton name")
        public void returnsSingletonName() {
            // Act
            StyleName sut = StyleName.of("abc");

            // Assert
            assertEquals("abc", sut.toString());
        }

        @Test
        @DisplayName("returns default name")
        public void returnsDefaultName() {
            // Act
            StyleName sut = StyleName.of();

            // Assert
            assertEquals("<default>", sut.toString());
        }

    }

}
