package mb.spoofax.compiler.cli;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.spoofax.compiler.util.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class CliParamRepr implements Serializable {
    interface Cases<R> {
        R option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description, @Nullable String converterCode);

        R positional(String paramId, int index, @Nullable String label, @Nullable String description, @Nullable String converterCode);
    }

    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description, @Nullable String converterCode) {
        return CliParamReprs.option(paramId, names, negatable, label, description, converterCode);
    }

    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description) {
        return CliParamReprs.option(paramId, names, negatable, label, description, null);
    }

    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label) {
        return CliParamReprs.option(paramId, names, negatable, label, null, null);
    }

    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable) {
        return CliParamReprs.option(paramId, names, negatable, null, null, null);
    }

    public static CliParamRepr option(String paramId, ListView<String> names) {
        return CliParamReprs.option(paramId, names, false, null, null, null);
    }

    public static CliParamRepr option(String paramId, String... names) {
        return CliParamReprs.option(paramId, ListView.of(names), false, null, null, null);
    }

    public static CliParamRepr positional(String paramId, int index, @Nullable String label, @Nullable String description, @Nullable String converterCode) {
        return CliParamReprs.positional(paramId, index, label, description, converterCode);
    }

    public static CliParamRepr positional(String paramId, int index, @Nullable String label, @Nullable String description) {
        return CliParamReprs.positional(paramId, index, label, description, null);
    }

    public static CliParamRepr positional(String paramId, int index, @Nullable String label) {
        return CliParamReprs.positional(paramId, index, label, null, null);
    }

    public static CliParamRepr positional(String paramId, int index) {
        return CliParamReprs.positional(paramId, index, null, null, null);
    }


    public abstract <R> R match(Cases<R> cases);

    public CliParamReprs.CaseOfMatchers.TotalMatcher_Option caseOf() {
        return CliParamReprs.caseOf(this);
    }

    public String getParamId() {
        return CliParamReprs.getParamId(this);
    }

    public @Nullable String getLabel() {
        return CliParamReprs.getLabel(this);
    }

    public @Nullable String getDescription() {
        return CliParamReprs.getDescription(this);
    }

    public @Nullable String getConverterCode() {
        return CliParamReprs.getConverterCode(this);
    }

    public String toJavaCode() {
        return caseOf()
            .option((paramId, names, negatable, label, description, converterCode) -> {
                final StringBuilder sb = new StringBuilder();
                sb.append("CliParam.option(");
                sb.append(StringUtil.doubleQuote(paramId));
                sb.append(", ListView.of(");
                boolean first = true;
                for(String name : names) {
                    if(!first) sb.append(", ");
                    first = false;
                    sb.append(StringUtil.doubleQuote(name));
                }
                sb.append("), ");
                sb.append(negatable);
                sb.append(", ");
                if(label != null) {
                    sb.append(StringUtil.doubleQuote(label));
                } else {
                    sb.append(StringUtil.doubleQuote(""));
                }
                sb.append(", ");
                if(description != null) {
                    sb.append(StringUtil.doubleQuote(description));
                } else {
                    sb.append("null");
                }
                sb.append(", ");
                if(converterCode != null) {
                    sb.append(converterCode);
                } else {
                    sb.append("null");
                }
                sb.append(")");
                return sb.toString();
            })
            .positional((paramId, index, label, description, converterCode) -> {
                final StringBuilder sb = new StringBuilder();
                sb.append("CliParam.positional(");
                sb.append(StringUtil.doubleQuote(paramId));
                sb.append(", ");
                sb.append(index);
                sb.append(", ");
                if(label != null) {
                    sb.append(StringUtil.doubleQuote(label));
                } else {
                    sb.append(StringUtil.doubleQuote(""));
                }
                sb.append(", ");
                if(description != null) {
                    sb.append(StringUtil.doubleQuote(description));
                } else {
                    sb.append("null");
                }
                sb.append(", ");
                if(converterCode != null) {
                    sb.append(converterCode);
                } else {
                    sb.append("null");
                }
                sb.append(")");
                return sb.toString();
            })
            ;
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
