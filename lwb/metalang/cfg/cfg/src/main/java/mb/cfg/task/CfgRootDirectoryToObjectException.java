package mb.cfg.task;

import mb.common.util.ADT;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

@ADT
public abstract class CfgRootDirectoryToObjectException extends Exception {
    public interface Cases<R> {
        R convertFail(CfgToObjectException cfgToObjectException, ResourceKey cfgFile, ResourceKey lockFile);

        R lockFileWriteFail(IOException ioException, ResourceKey cfgFile, ResourceKey lockfile);
    }

    public static CfgRootDirectoryToObjectException convertFail(
        CfgToObjectException cfgToObjectException,
        ResourceKey cfgFile,
        ResourceKey lockFile
    ) {
        return withCause(CfgRootDirectoryToObjectExceptions.convertFail(
            cfgToObjectException,
            cfgFile,
            lockFile
        ), cfgToObjectException);
    }

    public static CfgRootDirectoryToObjectException lockFileWriteFail(IOException ioException, ResourceKey cfgFile, ResourceKey lockFile) {
        return withCause(CfgRootDirectoryToObjectExceptions.lockFileWriteFail(ioException, cfgFile, lockFile), ioException);
    }


    private static CfgRootDirectoryToObjectException withCause(CfgRootDirectoryToObjectException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgRootDirectoryToObjectExceptions.CasesMatchers.TotalMatcher_ConvertFail cases() {
        return CfgRootDirectoryToObjectExceptions.cases();
    }

    public CfgRootDirectoryToObjectExceptions.CaseOfMatchers.TotalMatcher_ConvertFail caseOf() {
        return CfgRootDirectoryToObjectExceptions.caseOf(this);
    }

    public ResourceKey getCfgFile() {
        return CfgRootDirectoryToObjectExceptions.getCfgFile(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .convertFail((e, cfgFile, lockFile) -> "Failed to convert CFG file '" + cfgFile + "' with lock file '" + lockFile + "' to a language compiler input object")
            .lockFileWriteFail((e, cfgFile, lockFile) -> "Failed to write to lock file '" + lockFile + "'")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
