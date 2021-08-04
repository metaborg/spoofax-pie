package mb.spoofax.core.language.testrunner;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container for multiple {@link TestSuiteResult}s.
 */
public class TestResults implements Serializable {

    public final List<TestSuiteResult> suites = new ArrayList<>();

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
        for(TestSuiteResult suites : suites) {
            i += suites.tests.size();
        }
        return i;
    }

    public void add(TestSuiteResult suiteRun) {
        suites.add(suiteRun);
        numFailed += suiteRun.numFailed();
        numPassed += suiteRun.numPassed();
    }

    public void addToStringBuilder(StringBuilder builder) {
        for (TestSuiteResult suite : suites) {
            suite.addToStringBuilder(builder);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final TestResults other = (TestResults)o;
        return this.suites.equals(other.suites);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suites);
    }

    @Override
    public String toString() {
        return "TestResults{suites=" + suites + "}";
    }
}
