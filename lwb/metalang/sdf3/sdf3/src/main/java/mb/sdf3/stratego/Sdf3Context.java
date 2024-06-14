package mb.sdf3.stratego;

import org.metaborg.util.tuple.Tuple2;

import mb.common.option.Option;

public class Sdf3Context {
    public final Option<String> strategoQualifier;
    public final Option<Tuple2<String, String>> placeholders;

    public Sdf3Context(Option<String> strategoQualifier, Option<Tuple2<String, String>> placeholders) {
        this.strategoQualifier = strategoQualifier;
        this.placeholders = placeholders;
    }
}
