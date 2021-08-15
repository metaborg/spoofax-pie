package mb.spoofax.core.language.taskdef;

import mb.common.editor.HoverResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

public class NoneHoverTaskDef extends NoneTaskDef<NoneHoverTaskDef.Args, HoverResult> {
    public static final class Args implements Serializable {
        public static Args Empty = new Args();
    }

    @Inject
    protected NoneHoverTaskDef(@Named("packageId") String idPrefix) {
        super(idPrefix);
    }
}
