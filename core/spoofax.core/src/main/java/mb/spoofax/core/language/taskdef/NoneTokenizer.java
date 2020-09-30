package mb.spoofax.core.language.taskdef;

import mb.common.option.Option;
import mb.common.token.Tokens;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;

import javax.inject.Inject;

public class NoneTokenizer extends NoneTaskDef<ResourceKey, Tokens<?>> {
    @Inject public NoneTokenizer() {}
}
