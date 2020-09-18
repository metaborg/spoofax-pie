package mb.spoofax.core.language.taskdef;

import mb.common.option.Option;
import mb.common.token.Tokens;
import mb.pie.api.Supplier;

import javax.inject.Inject;

public class NullTokenizer extends NullTaskDef<Supplier<String>, Option<? extends Tokens<?>>> {
    @Inject public NullTokenizer() {}
}
