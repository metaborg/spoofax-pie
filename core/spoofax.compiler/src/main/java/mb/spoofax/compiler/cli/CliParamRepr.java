package mb.spoofax.compiler.cli;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.spoofax.compiler.util.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * A CLI parameter representation.
 */
@ADT
public abstract class CliParamRepr implements Serializable {

    interface Cases<R> {
        R option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description, @Nullable String converterCode);

        R positional(String paramId, int index, @Nullable String label, @Nullable String description, @Nullable String converterCode);
    }

    /**
     * Creates a CLI option representation.
     *
     * Single letter option names have to be preceded by a single dash ({@code -}).
     * Long option names have to be preceded by a double dash ({@code --}).
     *
     * @param paramId the parameter ID, which corresponds to the parameter ID of the command
     * @param names a list of short and long names of the option, preceded by single dash and double dash respectively
     * @param negatable whether a negated version of the option should be provided
     * @param label the label of the option, as shown in the help info; or {@code null} to specify no label
     * @param description the description of the option, as shown in the help info; or {@code null} to specify no description
     * @param converterCode the literal Java code to use for converting the argument string to an argument value; or {@code null} to specify none
     * @return the created CLI option representation
     */
    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description, @Nullable String converterCode) {
        return CliParamReprs.option(paramId, names, negatable, label, description, converterCode);
    }

    /**
     * Creates a CLI option representation.
     *
     * @see #option(String, ListView, boolean, String, String, String)
     */
    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description) {
        return CliParamReprs.option(paramId, names, negatable, label, description, null);
    }

    /**
     * Creates a CLI option representation.
     *
     * @see #option(String, ListView, boolean, String, String, String)
     */
    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable, @Nullable String label) {
        return CliParamReprs.option(paramId, names, negatable, label, null, null);
    }

    /**
     * Creates a CLI option representation.
     *
     * @see #option(String, ListView, boolean, String, String, String)
     */
    public static CliParamRepr option(String paramId, ListView<String> names, boolean negatable) {
        return CliParamReprs.option(paramId, names, negatable, null, null, null);
    }

    /**
     * Creates a CLI option representation.
     *
     * @see #option(String, ListView, boolean, String, String, String)
     */
    public static CliParamRepr option(String paramId, ListView<String> names) {
        return CliParamReprs.option(paramId, names, false, null, null, null);
    }

    /**
     * Creates a CLI option representation.
     *
     * @see #option(String, ListView, boolean, String, String, String)
     */
    public static CliParamRepr option(String paramId, String... names) {
        return CliParamReprs.option(paramId, ListView.of(names), false, null, null, null);
    }

    /**
     * Creates a CLI positional parameter representation.
     *
     * @param paramId the parameter ID, which corresponds to the parameter ID of the command
     * @param index the zero-based position index of the parameter
     * @param label the label of the parameter, as shown in the help info; or {@code null} to specify no label
     * @param description the description of the parameter, as shown in the help info; or {@code null} to specify no description
     * @param converterCode the literal Java code to use for converting the argument string to an argument value; or {@code null} to specify none
     * @return the created CLI parameter representation
     */
    public static CliParamRepr positional(String paramId, int index, @Nullable String label, @Nullable String description, @Nullable String converterCode) {
        return CliParamReprs.positional(paramId, index, label, description, converterCode);
    }

    /**
     * Creates a CLI positional parameter representation.
     *
     * @see #positional(String, int, String, String, String)
     */
    public static CliParamRepr positional(String paramId, int index, @Nullable String label, @Nullable String description) {
        return CliParamReprs.positional(paramId, index, label, description, null);
    }

    /**
     * Creates a CLI positional parameter representation.
     *
     * @see #positional(String, int, String, String, String)
     */
    public static CliParamRepr positional(String paramId, int index, @Nullable String label) {
        return CliParamReprs.positional(paramId, index, label, null, null);
    }

    /**
     * Creates a CLI positional parameter representation.
     *
     * @see #positional(String, int, String, String, String)
     */
    public static CliParamRepr positional(String paramId, int index) {
        return CliParamReprs.positional(paramId, index, null, null, null);
    }

    /**
     * Matches this object against the specified case matcher.
     *
     * @param cases the cases that are distinguised
     * @param <R> the type of return value
     * @return the return value
     */
    public abstract <R> R match(Cases<R> cases);

    public CliParamReprs.CaseOfMatchers.TotalMatcher_Option caseOf() {
        return CliParamReprs.caseOf(this);
    }

    /**
     * Gets the parameter ID.
     *
     * The parameter ID should match the corresponding task parameter ID.
     *
     * @return the parameter ID
     */
    public String getParamId() {
        return CliParamReprs.getParamId(this);
    }

    /**
     * Gets the label, as shown in the help info.
     *
     * @return the label; or {@code null}
     */
    public @Nullable String getLabel() {
        return CliParamReprs.getLabel(this);
    }

    /**
     * Gets the description, as shown in the help info.
     *
     * @return the description; or {@code null}
     */
    public @Nullable String getDescription() {
        return CliParamReprs.getDescription(this);
    }

    /**
     * Gets the literal Java type converter code.
     *
     * @return the converter code; or {@code null}
     */
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
