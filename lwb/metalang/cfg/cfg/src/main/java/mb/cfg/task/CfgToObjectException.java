package mb.cfg.task;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class CfgToObjectException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R astSupplyFail(Exception astSupplyException);

        R propertiesSupplyFail(Exception propertiesSupplyException);

        R validationFail(KeyedMessages messages);
    }

    public static CfgToObjectException astSupplyFail(Exception astSupplyException) {
        return withCause(CfgToObjectExceptions.astSupplyFail(astSupplyException), astSupplyException);
    }

    public static CfgToObjectException propertiesSupplyFail(Exception propertiesSupplyException) {
        return withCause(CfgToObjectExceptions.propertiesSupplyFail(propertiesSupplyException), propertiesSupplyException);
    }

    public static CfgToObjectException validationFail(KeyedMessages messages) {
        return CfgToObjectExceptions.validationFail(messages);
    }

    private static CfgToObjectException withCause(CfgToObjectException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgToObjectExceptions.CasesMatchers.TotalMatcher_AstSupplyFail cases() {
        return CfgToObjectExceptions.cases();
    }

    public CfgToObjectExceptions.CaseOfMatchers.TotalMatcher_AstSupplyFail caseOf() {
        return CfgToObjectExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .astSupplyFail((e) -> "Failed to supply AST of CFG language")
            .propertiesSupplyFail((e) -> "Failed to supply configuration properties")
            .validationFail((m) -> "Failed to build configuration objects; validation produced errors")
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
