package mb.tego.strategies;

import mb.tego.sequences.Seq;
import mb.tego.strategies.runtime.TegoEngine;

import java.util.concurrent.atomic.AtomicInteger;

public class MyTestStrategy extends NamedStrategy<String, Seq<String>> {
    public final AtomicInteger doEvalCalls = new AtomicInteger();

    @Override
    public Seq<String> evalInternal(TegoEngine engine, String input) {
        doEvalCalls.incrementAndGet();
        return Seq.from(() -> input);
    }

    @Override
    public String getName() {
        return "my-test-strategy";
    }

    @Override
    public String getParamName(int index) {
        throw new IndexOutOfBoundsException("Index " + index + " out of bounds.");
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
