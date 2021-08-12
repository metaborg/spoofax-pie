package mb.spoofax.core.language.taskdef;

import mb.common.editor.ReferenceResolutionResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

public class NoneResolveTaskDef extends NoneTaskDef<NoneResolveTaskDef.Args, ReferenceResolutionResult> {
    public static final class Args implements Serializable {
        public static Args Empty = new Args();
    }

    @Inject
    protected NoneResolveTaskDef(@Named("packageId") String idPrefix) {
        super(idPrefix);
    }
}
