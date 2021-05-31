package mb.spoofax.core.language.taskdef;

import mb.common.token.Tokens;
import mb.resource.ResourceKey;

import javax.inject.Inject;
import javax.inject.Named;

public class NoneTokenizer extends NoneTaskDef<ResourceKey, Tokens<?>> {
    @Inject public NoneTokenizer(@Named("packageId") String packageId) { super(packageId); }
}
