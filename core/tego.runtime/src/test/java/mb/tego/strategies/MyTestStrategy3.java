package mb.tego.strategies;

import mb.tego.sequences.Seq;
import mb.tego.strategies.runtime.TegoEngine;

import java.util.concurrent.atomic.AtomicInteger;

public class MyTestStrategy3 extends NamedStrategy3<String, String, String, String, Seq<String>> {
    public final AtomicInteger doEvalCalls = new AtomicInteger();

    @Override
    public Seq<String> evalInternal(TegoEngine engine, String part1, String part2, String part3, String input) {
        doEvalCalls.incrementAndGet();
        return Seq.fromOnce(() -> part1 + part2 + part3 + input);
    }

    @Override
    public String getName() {
        return "my-test-strategy-3";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "part1";
            case 1: return "part2";
            case 2: return "part3";
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
