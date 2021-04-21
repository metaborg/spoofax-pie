package mb.str.incr;

import mb.stratego.build.util.IOAgentTracker;
import mb.stratego.common.StrategoIOAgent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.library.IOAgent;

import java.io.ByteArrayOutputStream;

public class StrategoIOAgentTracker implements IOAgentTracker {
    private final StrategoIOAgent ioAgent;
    private final @Nullable ByteArrayOutputStream stdoutLog;
    private final @Nullable ByteArrayOutputStream stderrLog;

    public StrategoIOAgentTracker(
        StrategoIOAgent ioAgent,
        @Nullable ByteArrayOutputStream stdoutLog,
        @Nullable ByteArrayOutputStream stderrLog
    ) {
        this.ioAgent = ioAgent;
        this.stdoutLog = stdoutLog;
        this.stderrLog = stderrLog;
    }

    @Override public IOAgent agent() {
        return ioAgent;
    }

    @Override public String stdout() {
        return stdoutLog != null ? stdoutLog.toString() : "";
    }

    @Override public String stderr() {
        return stderrLog != null ? stderrLog.toString() : "";
    }
}
