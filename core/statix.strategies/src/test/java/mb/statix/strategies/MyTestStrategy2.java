package mb.statix.strategies;

import mb.statix.sequences.Seq;

import java.util.concurrent.atomic.AtomicInteger;

public class MyTestStrategy2 extends NamedStrategy2<Object, String, String, String, String> {
    public final AtomicInteger doEvalCalls = new AtomicInteger();

    @Override
    public Seq<String> eval(Object ctx, String part1, String part2, String input) {
        doEvalCalls.incrementAndGet();
        return Seq.fromOnce(() -> part1 + part2 + input);
    }

    @Override
    public String getName() {
        return "my-test-strategy-2";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "part1";
            case 1: return "part2";
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
