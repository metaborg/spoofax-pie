package mb.spoofax.core.language.testrunner;

import mb.common.message.KeyedMessages;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * Represents a testcase that is being run.
 *
 * Used by the Eclipse UI part of the runner. It notifies its parent (if any) when it gets the result for the test whose
 * run it represents.
 */
public class TestCaseRun {

    public final TestSuiteRun parent;
    public final String description;

    @Nullable private KeyedMessages messages;

    // keep track of the duration here
    // maybe at some point we will move that job to mbt.core
    private long start;
    private long duration = -1;

    /**
     * Create a TestCaseRun, representing a run of an ITestCase.
     *  @param parent
     *            the parent test suite (may be null).
     *  @param description
     *            Name of the test case
     */
    public TestCaseRun(TestSuiteRun parent, String description) {
        this.parent = parent;
        this.description = description;
        this.start = System.currentTimeMillis();
    }

    /**
     * Signals that you want to start the run.
     *
     * Only required if you care about timing.
     */
    public void start() {
        this.start = System.currentTimeMillis();
    }

    /**
     * Finish this test case run.
     *
     * We will update our parent (if any) with the result.
     *
     * @param messages
     *            the result of running this test case.
     */
    public void finish(@Nullable KeyedMessages messages) {
        this.duration = System.currentTimeMillis() - start;
        this.messages = messages;
        if(messages != null) {
            if(!messages.containsError()) {
                parent.pass();
            } else {
                parent.fail();
            }
        }
    }

    /**
     * The result of running this test case.
     *
     * May be null, if the run wasn't finished yet.
     */
    @Nullable
    public KeyedMessages result() {
        return messages;
    }

    /**
     * The time (in ms) that passed between the last call to {@link #start()} and the last call to
     * {@link #finish(KeyedMessages)}.
     */
    public long duration() {
        return duration;
    }

    public void addToStringBuilder(StringBuilder builder) {
        builder.append(this.description);
        if(messages != null) {
            if(!messages.containsError()) {
                builder.append(": PASS\n");
            } else {
                builder.append(": FAIL\n");
            }
            messages.addToStringBuilder(builder);
        } else {
            builder.append(": FAIL\n");
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final TestCaseRun other = (TestCaseRun)o;
        return this.description.equals(other.description)
            && Objects.equals(this.messages, other.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, messages);
    }

    @Override
    public String toString() {
        return "TestCaseRun{description=" + description + "}";
    }
}
