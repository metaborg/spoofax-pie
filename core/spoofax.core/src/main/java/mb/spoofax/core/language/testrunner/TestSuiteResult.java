package mb.spoofax.core.language.testrunner;

import java.io.Serializable;
import java.util.Objects;

import mb.common.message.KeyedMessages;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents running all the tests in a test suite.
 */
public class TestSuiteResult implements Serializable {

    public final String name;
    public final ResourceKey file;
    public final KeyedMessages messages;
    public final ListView<TestCaseResult> testCases;

    public final int numFailed;
    public final int numPassed;

    /**
     * Create a TestSuiteRun to represent the running of all tests in the test suite with the given name.
     *  @param messages
     *            messages generated during evaluation of the test suite file
     * @param file
     *            the file containing the module (required so double clicking opens the module)
     * @param name
     *            name of the test suite
     * @param testCases
     *            ListView containing the results of the testcases for this testsuite
     */
    public TestSuiteResult(KeyedMessages messages, ResourceKey file, String name, ListView<TestCaseResult> testCases) {
        this.messages = messages;
        this.file = file;
        this.name = name;
        this.testCases = testCases;
        int failed = 0;
        for(TestCaseResult testCase : this.testCases) {
            if (testCase.messages.containsError())
                failed += 1;
        }
        this.numFailed = failed;
        this.numPassed = testCases.size() - numFailed;
    }

    /**
     * Create a TestSuiteRun to represent the running of all tests in the test suite with the given name.
     * The name of the test suite is inferred from the ResourceKey.
     * Used as fallback if the testsuite can't be processed
     *
     * @param messages
     *            messages generated during evaluation of the test suite file
     * @param file
     *            the file containing the module (required so double clicking opens the module)
     */
    public TestSuiteResult(KeyedMessages messages, ResourceKey file) {
        this.messages = messages;
        this.file = file;
        this.name = file.getIdAsString();
        this.testCases = ListView.of();
        this.numFailed = 0;
        this.numPassed = 0;
    }

    public void addToStringBuilder(StringBuilder builder) {
        builder
            .append("TestSuite ")
            .append(this.name)
            .append('\n');
        messages.addToStringBuilder(builder);
        builder
            .append(testCases.size())
            .append(" tests\n");
        for (TestCaseResult test : testCases) {
            test.addToStringBuilder(builder);
        }
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TestSuiteResult that = (TestSuiteResult)o;
        return numFailed == that.numFailed && numPassed == that.numPassed && name.equals(that.name) && file.equals(that.file) && messages.equals(that.messages) && testCases.equals(that.testCases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, file, messages, testCases, numFailed, numPassed);
    }

    @Override
    public String toString() {
        return "TestSuiteResult{name=" + name + ", tests=" + testCases + ", file=" + file + ", passed=" + numPassed + ", failed=" + numFailed + "}";
    }
}
