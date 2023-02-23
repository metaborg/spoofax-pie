package mb.tego.strategies;

import mb.tego.sequences.Seq;
import mb.tego.strategies.runtime.TegoEngine;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class MyTestStrategy1 extends NamedStrategy1<String, String, Seq<String>> {
    public final AtomicInteger doEvalCalls = new AtomicInteger();

    @Override
    public Seq<String> evalInternal(TegoEngine engine, String part1, String input) {
        doEvalCalls.incrementAndGet();
        return Seq.from(() -> part1 + input);
    }

    @Override
    public String getName() {
        return "my-test-strategy-1";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "part1";
            default: throw new IndexOutOfBoundsException("Index " + index + " out of bounds.");
        }
    }

    @Override
    public int getPrecedence() {
        return 42;
    }

    @Override
    public void writeArg(StringBuilder sb, int index, Object arg) {
        sb.append(index).append(": ");    // Prefix added for testing
        super.writeArg(sb, index, arg);
    }
}
