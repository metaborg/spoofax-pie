package mb.cfg.task;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.stratego.common.StrategoException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class CfgToObjectException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R analyzeFail(KeyedMessages messages);

        R analyzeExceptionalFail(Exception astSupplyException);

        R normalizationFail(StrategoException normalizationException);

        R propertiesSupplyFail(Exception propertiesSupplyException);

        R buildConfigObjectFail(IllegalStateException illegalStateException);
    }

    public static CfgToObjectException analyzeFail(KeyedMessages messages) {
        return CfgToObjectExceptions.analyzeFail(messages);
    }

    public static CfgToObjectException analyzeExceptionalFail(Exception analysisOutputSupplyException) {
        return withCause(CfgToObjectExceptions.analyzeExceptionalFail(analysisOutputSupplyException), analysisOutputSupplyException);
    }

    public static CfgToObjectException normalizationFail(StrategoException normalizationException) {
        return CfgToObjectExceptions.normalizationFail(normalizationException);
    }

    public static CfgToObjectException propertiesSupplyFail(Exception propertiesSupplyException) {
        return withCause(CfgToObjectExceptions.propertiesSupplyFail(propertiesSupplyException), propertiesSupplyException);
    }

    public static CfgToObjectException buildConfigObjectFail(IllegalStateException illegalStateException) {
        return withCause(CfgToObjectExceptions.buildConfigObjectFail(illegalStateException), illegalStateException);
    }

    private static CfgToObjectException withCause(CfgToObjectException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgToObjectExceptions.CasesMatchers.TotalMatcher_AnalyzeFail cases() {
        return CfgToObjectExceptions.cases();
    }

    public CfgToObjectExceptions.CaseOfMatchers.TotalMatcher_AnalyzeFail caseOf() {
        return CfgToObjectExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .analyzeFail((e) -> "Analysis produced errors")
            .analyzeExceptionalFail((e) -> "Analysis failed unexpectedly")
            .normalizationFail((e) -> "Normalization failed unexpectedly")
            .propertiesSupplyFail((e) -> "Failed to supply configuration properties")
            .buildConfigObjectFail((e) -> "Failed to build configuration object")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return CfgToObjectExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
