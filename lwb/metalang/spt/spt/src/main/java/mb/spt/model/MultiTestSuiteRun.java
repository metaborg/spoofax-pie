package mb.spt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for multiple {@link TestSuiteRun}s.
 */
public class MultiTestSuiteRun implements Serializable {

    public final List<TestSuiteRun> suites = new ArrayList<>();

    private int numFailed = 0;
    private int numPassed = 0;

    /**
     * The number of test cases that failed.
     *
     * Accumulated over all our test suites.
     */
    public int numFailed() {
        return numFailed;
    }

    /**
     * The number of test cases that passed.
     *
     * Accumulated over all our test suites.
     */
    public int numPassed() {
        return numPassed;
    }

    /**
     * Our children should notify whenever they have a failing test case run
     */
    protected void fail() {
        numFailed++;
    }

    /**
     * Our children should notify whenever they have a passing test case run
     */
    protected void pass() {
        numPassed++;
    }

    /**
     * The number of test cases of all our children combined.
     */
    public int numTests() {
        int i = 0;
        for(TestSuiteRun suites : suites) {
            i += suites.tests.size();
        }
        return i;
    }

    public void add(TestSuiteRun suiteRun) {
        suites.add(suiteRun);
        numFailed += suiteRun.numFailed();
        numPassed += suiteRun.numPassed();
    }

    public String toLog() {
        StringBuilder builder = new StringBuilder();
        for (TestSuiteRun suite : suites) {
            builder.append(suite.toLog());
        }
        return builder.toString();
    }

    // TODO: Add equality, hash and toString functions
}
