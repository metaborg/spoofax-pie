package mb.spoofax.core.language.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mb.common.message.KeyedMessages;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents running all the tests in a test suite.
 */
public class TestSuiteRun implements Serializable {

    @Nullable public final MultiTestSuiteRun parent;
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
        this(messages, file, null, name);
    }

    /**
     * Create a TestSuiteRun to represent the running of all tests in the test suite with the given name.
     *
     * @param file
     *            the file containing the module (required so double clicking opens the module)
     * @param parent
     *            if you want to run multiple test suites, you can group them in a {@link MultiTestSuiteRun} to keep
     *            track of them.
     * @param name
     *            the name of the test suite.
     */
    public TestSuiteRun(@Nullable KeyedMessages messages, ResourceKey file,
                        @Nullable MultiTestSuiteRun parent, String name) {
        this.messages = messages;
        this.file = file;
        this.parent = parent;
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

    public String toLog() {
        StringBuilder builder = new StringBuilder();
        builder
            .append("TestSuite ")
            .append(this.name)
            .append('\n')
            .append(tests.size())
            .append(" tests\n");
        for (TestCaseRun test : tests) {
            builder.append(test.toLog());
        }
        return builder.toString();
    }

    // TODO: Add equality, hash and toString functions
}
