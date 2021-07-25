package mb.spt.model;

import mb.common.message.KeyedMessages;

import javax.annotation.Nullable;

/**
 * Represents a testcase that is being run.
 *
 * Used by the Eclipse UI part of the runner. It notifies its parent (if any) when it gets the result for the test whose
 * run it represents.
 */
public class TestCaseRun {

    public final TestSuiteRun parent;
    public final TestCase test;

    @Nullable private KeyedMessages messages;
    private boolean success;
    // keep track of the duration here
    // maybe at some point we will move that job to mbt.core
    private long start;
    private long duration = -1;

    /**
     * Create a TestCaseRun, representing a run of an ITestCase.
     *
     * @param parent
     *            the parent test suite (may be null).
     * @param test
     *            the test that you will run.
     */
    public TestCaseRun(TestSuiteRun parent, TestCase test) {
        this.parent = parent;
        this.test = test;
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

    public String toLog() {
        if(messages != null && !messages.containsError()) {
            return this.test.description + ": PASS\n";
        } else {
            return this.test.description + ": FAIL\n";
        }
    }
    // TODO: Add equality, hash and toString functions
}
