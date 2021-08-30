package mb.statix.utils;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link CaseFormat} class.
 */
public final class CaseFormatTests {

    @TestFactory
    public Stream<DynamicTest> splitCamelCase() {
        return Stream.of(
            TestCase.of("", new String[]{ "" }),
            TestCase.of("foobar", new String[]{ "foobar" }),
            TestCase.of("FooBar", new String[]{ "Foo", "Bar" }),
            TestCase.of("PDFReader", new String[]{ "PDF", "Reader" }),
            TestCase.of("fooBar42Qix", new String[]{ "foo", "Bar", "42", "Qix" })
        ).map(tc -> DynamicTest.dynamicTest("splits " + tc.getInput() + " into " + Arrays.toString(tc.getExpected()), () -> {
            // Arrange

            // Act
            final String[] actual = CaseFormat.splitCamelCase(tc.getInput());

            // Assert
            assertArrayEquals(tc.getExpected(), actual);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> combineKebabCase() {
        return Stream.of(
            TestCase.of(new String[0], ""),
            TestCase.of(new String[]{ "" }, ""),
            TestCase.of(new String[]{ "foobar" }, "foobar"),
            TestCase.of(new String[]{ "Foo", "Bar" }, "foo-bar"),
            TestCase.of(new String[]{ "PDF", "Reader" }, "pdf-reader"),
            TestCase.of(new String[]{ "foo", "Bar", "42", "Qix" }, "foo-bar-42-qix")
        ).map(tc -> DynamicTest.dynamicTest("combines " + Arrays.toString(tc.getInput()) + " into " + tc.getExpected(), () -> {
            // Arrange

            // Act
            final String actual = CaseFormat.combineKebabCase(tc.getInput());

            // Assert
            assertEquals(tc.getExpected(), actual);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> combineSnakeCase() {
        return Stream.of(
            TestCase.of(new String[0], ""),
            TestCase.of(new String[]{ "" }, ""),
            TestCase.of(new String[]{ "foobar" }, "foobar"),
            TestCase.of(new String[]{ "Foo", "Bar" }, "foo_bar"),
            TestCase.of(new String[]{ "PDF", "Reader" }, "pdf_reader"),
            TestCase.of(new String[]{ "foo", "Bar", "42", "Qix" }, "foo_bar_42_qix")
        ).map(tc -> DynamicTest.dynamicTest("combines " + Arrays.toString(tc.getInput()) + " into " + tc.getExpected(), () -> {
            // Arrange

            // Act
            final String actual = CaseFormat.combineSnakeCase(tc.getInput());

            // Assert
            assertEquals(tc.getExpected(), actual);
        }));
    }

}
