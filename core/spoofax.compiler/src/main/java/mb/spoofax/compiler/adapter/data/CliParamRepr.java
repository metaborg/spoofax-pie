package mb.spoofax.compiler.adapter.data;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.spoofax.compiler.util.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

@ADT
public abstract class CliParamRepr implements Serializable {
    public interface Cases<R> {
        R option(String paramId, Option option);

        R positional(String paramId, Positional positional);
    }

    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description, @Nullable String converterCode) {
        return CliParamReprs.option(paramId, new Option(names, negatable, label, description, converterCode));
    }

    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description) {
        return CliParamReprs.option(paramId, new Option(names, negatable, label, description, null));
    }

    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label) {
        return CliParamReprs.option(paramId, new Option(names, negatable, label, null, null));
    }

    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable) {
        return CliParamReprs.option(paramId, new Option(names, negatable, null, null, null));
    }

    public static CliParamRepr option(String paramId, ListView<String> names) {
        return CliParamReprs.option(paramId, new Option(names, false, null, null, null));
    }

    public static CliParamRepr option(String paramId, String... names) {
        return CliParamReprs.option(paramId, new Option(ListView.of(names), false, null, null, null));
    }

    public static CliParamRepr positional(String paramId, int index, @Nullable String label, @Nullable String description, @Nullable String converterCode) {
        return CliParamReprs.positional(paramId, new Positional(index, label, description, converterCode));
    }

    public static CliParamRepr positional(String paramId, int index, @Nullable String label, @Nullable String description) {
        return CliParamReprs.positional(paramId, new Positional(index, label, description, null));
    }

    public static CliParamRepr positional(String paramId, int index, @Nullable String label) {
        return CliParamReprs.positional(paramId, new Positional(index, label, null, null));
    }

    public static CliParamRepr positional(String paramId, int index) {
        return CliParamReprs.positional(paramId, new Positional(index, null, null, null));
    }


    public abstract <R> R match(Cases<R> cases);

    public CliParamReprs.CaseOfMatchers.TotalMatcher_Option caseOf() {
        return CliParamReprs.caseOf(this);
    }

    public String getParamId() {
        return CliParamReprs.getParamId(this);
    }

    public @Nullable String getLabel() {
        return caseOf().option((id, o) -> o.label).otherwise_(null);
    }

    public @Nullable String getDescription() {
        return caseOf().option((id, o) -> o.description).positional((id, o) -> o.description);
    }

    public @Nullable String getConverterCode() {
        return caseOf().option((id, o) -> o.converterCode).positional((id, o) -> o.converterCode);
    }

    public String toJavaCode() {
        return caseOf()
            .option((paramId, option) -> {
                final StringBuilder sb = new StringBuilder();
                sb.append("CliParam.option(");
                sb.append(StringUtil.doubleQuote(paramId));
                sb.append(", ListView.of(");
                boolean first = true;
                for(String name : option.names) {
                    if(!first) sb.append(", ");
                    first = false;
                    sb.append(StringUtil.doubleQuote(name));
                }
                sb.append("), ");
                sb.append(option.negatable);
                sb.append(", ");
                if(option.label != null) {
                    sb.append(StringUtil.doubleQuote(option.label));
                } else {
                    sb.append(StringUtil.doubleQuote(""));
                }
                sb.append(", ");
                if(option.description != null) {
                    sb.append(StringUtil.doubleQuote(option.description));
                } else {
                    sb.append("null");
                }
                sb.append(", ");
                if(option.converterCode != null) {
                    sb.append(option.converterCode);
                } else {
                    sb.append("null");
                }
                sb.append(")");
                return sb.toString();
            })
            .positional((paramId, positional) -> {
                final StringBuilder sb = new StringBuilder();
                sb.append("CliParam.positional(");
                sb.append(StringUtil.doubleQuote(paramId));
                sb.append(", ");
                sb.append(positional.index);
                sb.append(", ");
                if(positional.label != null) {
                    sb.append(StringUtil.doubleQuote(positional.label));
                } else {
                    sb.append(StringUtil.doubleQuote(""));
                }
                sb.append(", ");
                if(positional.description != null) {
                    sb.append(StringUtil.doubleQuote(positional.description));
                } else {
                    sb.append("null");
                }
                sb.append(", ");
                if(positional.converterCode != null) {
                    sb.append(positional.converterCode);
                } else {
                    sb.append("null");
                }
                sb.append(")");
                return sb.toString();
            })
            ;
    }


    public static class Option {
        public final ListView<String> names;
        public final boolean negatable;
        public final @Nullable String label;
        public final @Nullable String description;
        public final @Nullable String converterCode;

        public Option(ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description, @Nullable String converterCode) {
            this.names = names;
            this.negatable = negatable;
            this.label = label;
            this.description = description;
            this.converterCode = converterCode;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Option option = (Option)o;
            return negatable == option.negatable &&
                names.equals(option.names) &&
                Objects.equals(label, option.label) &&
                Objects.equals(description, option.description) &&
                Objects.equals(converterCode, option.converterCode);
        }

        @Override public int hashCode() {
            return Objects.hash(names, negatable, label, description, converterCode);
        }

        @Override public String toString() {
            return "Option{" +
                "names=" + names +
                ", negatable=" + negatable +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", converterCode='" + converterCode + '\'' +
                '}';
        }
    }

    public static class Positional {
        public final int index;
        public final @Nullable String label;
        public final @Nullable String description;
        public final @Nullable String converterCode;

        public Positional(int index, @Nullable String label, @Nullable String description, @Nullable String converterCode) {
            this.index = index;
            this.label = label;
            this.description = description;
            this.converterCode = converterCode;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Positional that = (Positional)o;
            return index == that.index &&
                Objects.equals(label, that.label) &&
                Objects.equals(description, that.description) &&
                Objects.equals(converterCode, that.converterCode);
        }

        @Override public int hashCode() {
            return Objects.hash(index, label, description, converterCode);
        }

        @Override public String toString() {
            return "Positional{" +
                "index=" + index +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", converterCode='" + converterCode + '\'' +
                '}';
        }
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
