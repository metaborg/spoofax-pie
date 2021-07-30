package mb.spoofax.core.language.testrunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mb.common.message.KeyedMessages;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents running all the tests in a test suite.
 */
public class TestSuiteRun implements Serializable {

    @Nullable public MultiTestSuiteRun parent = null;
    public final String name;
    public final List<TestCaseRun> tests = new ArrayList<>();
    public final ResourceKey file;
    @Nullable public KeyedMessages messages;

    private int numFailed = 0;
    private int numPassed = 0;

    /**
     * Create a TestSuiteRun to represent the running of all tests in the test suite with the given name.
     *
     * @param messages
     *            messages generated during evaluation of the test suite file
     * @param file
     *            the file containing the module (required so double clicking opens the module)
     * @param name
     *            the name of the test suite.
     */
    public TestSuiteRun(@Nullable KeyedMessages messages, ResourceKey file, String name) {
        this.messages = messages;
        this.file = file;
        this.name = name;
    }

    /**
     * The number of failed test case runs (so far) in this test suite.
     */
    public int numFailed() {
        return numFailed;
    }

    /**
     * The number of successfully completed test case runs (so far) in this test suite.
     */
    public int numPassed() {
        return numPassed;
    }

    /**
     * Called by our kids to notify us that they failed.
     */
    protected void fail() {
        numFailed++;
        if(parent != null) {
            parent.fail();
        }
    }

    /**
     * Called by our kids to notify us that they passed.
     */
    protected void pass() {
        numPassed++;
        if(parent != null) {
            parent.pass();
        }
    }

    public void addToStringBuilder(StringBuilder builder) {
        builder
            .append("TestSuite ")
            .append(this.name)
            .append('\n');
        if(messages != null) {
            messages.addToStringBuilder(builder);
        }
        builder
            .append(tests.size())
            .append(" tests\n");
        for (TestCaseRun test : tests) {
            test.addToStringBuilder(builder);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final TestSuiteRun other = (TestSuiteRun)o;
        return this.name.equals(other.name)
            && this.tests.equals(other.tests)
            && this.file.equals(other.file)
            && Objects.equals(this.messages, other.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tests, file, messages);
    }

    @Override
    public String toString() {
        return "TestSuiteRun{name=" + name + ", tests=" + tests + ", file=" + file + "}";
    }
}
