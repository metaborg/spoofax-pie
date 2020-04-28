package mb.common.region;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the {@link Region} class. */
@SuppressWarnings("CodeBlock2Expr")
@DisplayName("Region")
public class RegionTests {

    /** Tests the {@link Region#atOffset(int)} function. */
    @DisplayName("atOffset(int)")
    @Nested public class AtOffsetTests {

        @Test
        @DisplayName("returns empty region with specified offset")
        public void returnsEmptyRegionWithSpecifiedOffset() {
            // Arrange
            int offset = 3;

            // Act
            Region sut = Region.atOffset(offset);

            // Assert
            assertEquals(offset, sut.getStartOffset());
            assertEquals(offset, sut.getEndOffset());
            assertTrue(sut.isEmpty());
        }

        @Test
        @DisplayName("when offset is negative, throws exception")
        public void whenOffsetIsNegative_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Region.atOffset(-2);
            });
        }

    }


    /** Tests the {@link Region#fromOffsets(int, int)} function. */
    @DisplayName("fromOffsets(int, int)")
    @Nested public class FromOffsetsTests {

        @Test
        @DisplayName("returns region with specified offsets")
        public void returnsRegionWithSpecifiedOffsets() {
            // Arrange
            int startOffset = 3;
            int endOffset = 4;

            // Act
            Region sut = Region.fromOffsets(startOffset, endOffset);

            // Assert
            assertEquals(startOffset, sut.getStartOffset());
            assertEquals(endOffset, sut.getEndOffset());
        }

        @Test
        @DisplayName("when start and end are equal, returns empty region")
        public void whenStartAndEndAreEqual_returnsEmptyRegion() {
            // Arrange
            int offset = 3;

            // Act
            Region sut = Region.fromOffsets(offset, offset);

            // Assert
            assertEquals(offset, sut.getStartOffset());
            assertEquals(offset, sut.getEndOffset());
            assertTrue(sut.isEmpty());
        }

        @Test
        @DisplayName("when end is before start, throws exception")
        public void whenEndIsBeforeStart_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Region.fromOffsets(10, 2);
            });
        }

        @Test
        @DisplayName("when start is negative, throws exception")
        public void whenStartIsNegative_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Region.fromOffsets(-2, 4);
            });
        }

    }


    /** Tests the {@link Region#fromOffsetLength(int, int)} function. */
    @DisplayName("fromOffsetLength(int, int)")
    @Nested public class FromOffsetLengthTests {

        @Test
        @DisplayName("returns region with specified offset and length")
        public void returnsRegionWithSpecifiedOffsetAndLength() {
            // Arrange
            int offset = 3;
            int length = 2;

            // Act
            Region sut = Region.fromOffsetLength(offset, length);

            // Assert
            assertEquals(offset, sut.getStartOffset());
            assertEquals(offset + length, sut.getEndOffset());
            assertEquals(length, sut.getLength());
        }

        @Test
        @DisplayName("when length is zero, returns empty region")
        public void whenLengthIsZero_returnsEmptyRegion() {
            // Arrange
            int offset = 3;
            int length = 0;

            // Act
            Region sut = Region.fromOffsetLength(offset, length);

            // Assert
            assertEquals(offset, sut.getStartOffset());
            assertEquals(offset, sut.getEndOffset());
            assertEquals(0, sut.getLength());
            assertTrue(sut.isEmpty());
        }

        @Test
        @DisplayName("when length is negative, throws exception")
        public void whenEndIsBeforeStart_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Region.fromOffsetLength(10, -8);
            });
        }

        @Test
        @DisplayName("when start is negative, throws exception")
        public void whenStartIsNegative_throwsException() {
            // Act/Assert
            assertThrows(IllegalArgumentException.class, () -> {
                Region.fromOffsetLength(-2, 6);
            });
        }

    }


    /** Tests the {@link Region#fromString(String)} function. */
    @DisplayName("fromString(String)")
    @Nested public class FromStringTests {

        @Test
        @DisplayName("when string is 'start-end', parses successfully")
        public void whenStringIsStartEnd_parsesSuccessfully() {
            // Act
            @Nullable Region sut = Region.fromString("3-7");

            // Assert
            assertNotNull(sut);
            assertEquals(3, sut.getStartOffset());
            assertEquals(7, sut.getEndOffset());
        }

        @Test
        @DisplayName("when string is 'offset-offset', parses successfully")
        public void whenStringIsOffsetOffset_parsesSuccessfully() {
            // Act
            @Nullable Region sut = Region.fromString("3-3");

            // Assert
            assertNotNull(sut);
            assertEquals(3, sut.getStartOffset());
            assertEquals(3, sut.getEndOffset());
            assertTrue(sut.isEmpty());
        }

        @Test
        @DisplayName("when string is 'offset', parses successfully")
        public void whenStringIsOffset_parsesSuccessfully() {
            // Act
            @Nullable Region sut = Region.fromString("3");

            // Assert
            assertNotNull(sut);
            assertEquals(3, sut.getStartOffset());
            assertEquals(3, sut.getEndOffset());
            assertTrue(sut.isEmpty());
        }

        @Test
        @DisplayName("when end is before start, returns null")
        public void whenEndIsBeforeStart_returnsNull() {
            // Act
            @Nullable Region sut = Region.fromString("3-1");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when format is invalid (1), returns null")
        public void whenFormatIsInvalid1_returnsNull() {
            // Act
            @Nullable Region sut = Region.fromString("-3");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when format is invalid (2), returns null")
        public void whenFormatIsInvalid2_returnsNull() {
            // Act
            @Nullable Region sut = Region.fromString("-");

            // Assert
            assertNull(sut);
        }

        @Test
        @DisplayName("when numbers cannot be parsed, returns null")
        public void whenNumbersCannotBeParsed_returnsNull() {
            // Act
            @Nullable Region sut = Region.fromString("aa-bb");

            // Assert
            assertNull(sut);
        }


        @Test
        @DisplayName("when number cannot be parsed, returns null")
        public void whenNumberCannotBeParsed_returnsNull() {
            // Act
            @Nullable Region sut = Region.fromString("aa");

            // Assert
            assertNull(sut);
        }

    }

    /** Tests the {@link Region#getStartOffset()} property. */
    @DisplayName("getStartOffset()")
    @Nested public class GetStartOffsetTests {

        @Test
        @DisplayName("returns start offset")
        public void returnsStartOffset() {
            // Act
            Region sut = Region.fromOffsets(3, 5);

            // Assert
            assertEquals(3, sut.getStartOffset());
        }

    }

    /** Tests the {@link Region#getEndOffset()} property. */
    @DisplayName("getEndOffset()")
    @Nested public class GetEndOffsetTests {

        @Test
        @DisplayName("returns end offset")
        public void returnsEndOffset() {
            // Act
            Region sut = Region.fromOffsets(3, 5);

            // Assert
            assertEquals(5, sut.getEndOffset());
        }

    }

    /** Tests the {@link Region#getLength()} property. */
    @DisplayName("getLength()")
    @Nested public class GetLengthTests {

        @Test
        @DisplayName("when region is not empty, returns non-zero length")
        public void whenRegionIsNotEmpty_returnsNonZeroLength() {
            // Act
            Region sut = Region.fromOffsets(3, 5);

            // Assert
            assertEquals(2, sut.getLength());
        }

        @Test
        @DisplayName("when region is empty, returns zero length")
        public void whenRegionIsEmpty_returnsZeroLength() {
            // Act
            Region sut = Region.fromOffsets(3, 3);

            // Assert
            assertEquals(0, sut.getLength());
        }

    }

    /** Tests the {@link Region#isEmpty()} property. */
    @DisplayName("isEmpty()")
    @Nested public class IsEmptyTests {

        @Test
        @DisplayName("when region is not empty, returns false")
        public void whenRegionIsNotEmpty_returnsFalse() {
            // Act
            Region sut = Region.fromOffsets(3, 5);

            // Assert
            assertFalse(sut.isEmpty());
        }

        @Test
        @DisplayName("when region is empty, returns true")
        public void whenRegionIsEmpty_returnsTrue() {
            // Act
            Region sut = Region.fromOffsets(3, 3);

            // Assert
            assertTrue(sut.isEmpty());
        }

    }

    private static Stream<Arguments> provideContainsRegionTestData() {
        return Stream.of(
            // @formatter:off
            /* (..[]..) contains (.[]...)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(1, 2), false, false),
            /* (..[]..) contains (..[]..)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(2, 3),  true,  true),
            /* (..[]..) contains (...[].)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(3, 4), false, false),
            /* (..[]..) contains (.[-]..)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(1, 3), false,  true),
            /* (..[]..) contains (..[-].)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(2, 4), false,  true),
            /* (..[]..) contains (.[--].)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(1, 4), false,  true),
            /* (..[]..) contains (.|....)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(1, 1), false, false),
            /* (..[]..) contains (..|...)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(2, 2),  true, false),
            /* (..[]..) contains (...|..)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(3, 3),  true, false),
            /* (..[]..) contains (....|.)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(4, 4), false, false)
            // @formatter:on
        );
    }

    private static Stream<Arguments> provideContainsIntTestData() {
        return Stream.of(
            // @formatter:off
            /* (..[.].) contains (.|....)? */ Arguments.of(Region.fromOffsets(2, 4), 1, false),
            /* (..[.].) contains (..|...)? */ Arguments.of(Region.fromOffsets(2, 4), 2,  true),
            /* (..[.].) contains (...|..)? */ Arguments.of(Region.fromOffsets(2, 4), 3,  true),
            /* (..[.].) contains (....|.)? */ Arguments.of(Region.fromOffsets(2, 4), 4, false),
            /* (..[.].) contains (....|.)? */ Arguments.of(Region.fromOffsets(2, 4), 5, false)
            // @formatter:on
        );
    }

    private static Stream<Arguments> provideIntersectionTestData() {
        return Stream.of(
            // @formatter:off
            /* intersectionOf (..[]..) and (.[]...)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(1, 2), Region.fromOffsets(2, 2)),
            /* intersectionOf (..[]..) and (..[]..)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(2, 3), Region.fromOffsets(2, 3)),
            /* intersectionOf (..[]..) and (...[].)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(3, 4), Region.fromOffsets(3, 3)),
            /* intersectionOf (..[]..) and (.[-]..)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(1, 3), Region.fromOffsets(2, 3)),
            /* intersectionOf (..[]..) and (..[-].)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(2, 4), Region.fromOffsets(2, 3)),
            /* intersectionOf (..[]..) and (.[--].)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(1, 4), Region.fromOffsets(2, 3)),
            /* intersectionOf (..[]..) and (.|....)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(1, 1),                     null),
            /* intersectionOf (..[]..) and (..|...)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(2, 2), Region.fromOffsets(2, 2)),
            /* intersectionOf (..[]..) and (...|..)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(3, 3), Region.fromOffsets(3, 3)),
            /* intersectionOf (..[]..) and (....|.)? */ Arguments.of(Region.fromOffsets(2, 3), Region.fromOffsets(4, 4),                     null)
            // @formatter:on
        );
    }


    /** Tests the {@link Region#contains(Region)} method. */
    @DisplayName("contains(Region)")
    @Nested public class ContainsTests {

        @ParameterizedTest(name = "region {0} contains {1}? {2}; region {1} contains {0}? {3}")
        @MethodSource("mb.common.region.RegionTests#provideContainsRegionTestData")
        public void regionContains(Region a, Region b, boolean expectAContainsB, boolean expectBContainsA) {
            // Act
            boolean aContainsB = a.contains(b);
            boolean bContainsA = b.contains(a);

            // Assert
            assertEquals(expectAContainsB, aContainsB);
            assertEquals(expectBContainsA, bContainsA);
        }

        @ParameterizedTest(name = "region {0} contains offset {1}? {2}")
        @MethodSource("mb.common.region.RegionTests#provideContainsIntTestData")
        public void offsetContains(Region a, int b, boolean expectAContainsB) {
            // Act
            boolean aContainsB = a.contains(b);

            // Assert
            assertEquals(expectAContainsB, aContainsB);
        }

    }

    /** Tests the {@link Region#intersectsWith(Region)} method. */
    @DisplayName("intersectsWith(Region)")
    @Nested public class IntersectsWithTests {

        @ParameterizedTest(name = "intersection of {0} and {1} = {2}")
        @MethodSource("mb.common.region.RegionTests#provideIntersectionTestData")
        public void regionIntersectsWith(Region a, Region b, @Nullable Region intersection) {
            // Act
            boolean aIntersectsB = a.intersectsWith(b);
            boolean bIntersectsA = b.intersectsWith(a);

            // Assert
            assertEquals(intersection != null, aIntersectsB);
            assertEquals(intersection != null, bIntersectsA);
        }

    }

    /** Tests the {@link Region#intersectionOf(Region, Region)} function. */
    @DisplayName("intersectionOf(Region, Region)")
    @Nested public class IntersectionOfTests {

        @ParameterizedTest(name = "intersection of {0} and {1} = {2}")
        @MethodSource("mb.common.region.RegionTests#provideIntersectionTestData")
        public void regionIntersectionOf(Region a, Region b, @Nullable Region intersection) {
            // Act
            @Nullable Region abIntersection = Region.intersectionOf(a, b);
            @Nullable Region baIntersection = Region.intersectionOf(b, a);

            // Assert
            assertEquals(intersection, abIntersection);
            assertEquals(intersection, baIntersection);
        }

    }



    /** Tests the contract of the {@link Region#equals(Object)} and {@link Region#hashCode()} methods. */
    @Test
    public void equalityContract() {
        EqualsVerifier.forClass(Region.class).verify();
    }

    /** Tests the {@link Region#toString()} method. */
    @DisplayName("toString()")
    @Nested public class ToStringTests {

        @Test
        @DisplayName("when region is not empty, returns 'start-end'")
        public void whenRegionIsNotEmpty_returnsStartDashEnd() {
            // Arrange
            Region sut = Region.fromOffsets(3, 5);

            // Act
            String str = sut.toString();

            // Assert
            assertEquals("3-5", str);
        }

        @Test
        @DisplayName("when region is empty, returns 'offset'")
        public void whenRegionIsEmpty_returnsOffset() {
            // Arrange
            Region sut = Region.fromOffsets(3, 3);

            // Act
            String str = sut.toString();

            // Assert
            assertEquals("3", str);
        }

    }

}
