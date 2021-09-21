package mb.statix.strategies;

import mb.statix.sequences.Seq;
import mb.statix.strategies.runtime.TegoEngine;

import java.util.concurrent.atomic.AtomicInteger;

public class MyTestStrategy extends NamedStrategy<String, Seq<String>> {
    public final AtomicInteger doEvalCalls = new AtomicInteger();

    @Override
    public Seq<String> evalInternal(TegoEngine engine, String input) {
        doEvalCalls.incrementAndGet();
        return Seq.fromOnce(() -> input);
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
