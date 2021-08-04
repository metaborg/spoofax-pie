package mb.spoofax.core.language.testrunner;

import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Container for multiple {@link TestSuiteResult}s.
 */
public class TestResults implements Serializable {

    public final ListView<TestSuiteResult> suites;

    public final int numFailed;
    public final int numPassed;

    public TestResults(ListView<TestSuiteResult> suites) {
        this.suites = suites;
        int failed = 0;
        int passed = 0;
        for(TestSuiteResult suite : suites) {
            failed += suite.numFailed;
            passed += suite.numPassed;
        }
        this.numFailed = failed;
        this.numPassed = passed;
    }

    /**
     * The number of test cases of all our children combined.
     */
    public int numTests() {
        return numFailed + numPassed;
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
        return "TestResults{suites=" + suites + ", passed=" + numPassed + ", failed=" + numFailed + "}";
    }
}
