package mb.spoofax.core.language.taskdef;

import mb.common.token.Tokens;
import mb.pie.api.Supplier;

import javax.inject.Inject;

public class NoneTokenizer extends NoneTaskDef<Supplier<String>, Tokens<?>> {
    @Inject public NoneTokenizer() {}
}
