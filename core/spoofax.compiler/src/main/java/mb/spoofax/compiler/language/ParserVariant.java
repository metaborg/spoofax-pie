package mb.spoofax.compiler.language;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class ParserVariant implements Serializable {
    public enum Jsglr2Preset implements Serializable {
        Standard,
        Elkhound,
        Recovery,
        RecoveryElkhound,
        DataDependent,
        LayoutSensitive,
        Composite,
        Incremental,
        IncrementalRecovery;

        public String toJsglr2PresetString() {
            switch(this) {
                default:
                case Standard:
                    return "standard";
                case Elkhound:
                    return "elkhound";
                case Recovery:
                    return "recovery";
                case RecoveryElkhound:
                    return "recoveryElkhound";
                case DataDependent:
                    return "dataDependent";
                case LayoutSensitive:
                    return "layoutSensitive";
                case Composite:
                    return "composite";
                case Incremental:
                    return "incremental";
                case IncrementalRecovery:
                    return "incrementalRecovery";
            }
        }
    }

    interface Cases<R> {
        R jsglr1();

        R jsglr2(Jsglr2Preset preset);
    }

    public static ParserVariant jsglr1() {
        return ParserVariants.jsglr1();
    }

    public static ParserVariant jsglr2() {
        return jsglr2(Jsglr2Preset.Recovery);
    }

    public static ParserVariant jsglr2(Jsglr2Preset preset) {
        return ParserVariants.jsglr2(preset);
    }


    public abstract <R> R match(Cases<R> cases);

    public boolean isJsglr1() {
        return caseOf().jsglr1_(true).otherwise_(false);
    }

    public boolean isJsglr2() {
        return caseOf().jsglr2_(true).otherwise_(false);
    }

    public static ParserVariants.CasesMatchers.TotalMatcher_Jsglr1 cases() {
        return ParserVariants.cases();
    }

    public ParserVariants.CaseOfMatchers.TotalMatcher_Jsglr1 caseOf() {
        return ParserVariants.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
