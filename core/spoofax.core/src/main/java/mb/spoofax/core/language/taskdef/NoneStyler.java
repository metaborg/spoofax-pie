package mb.spoofax.core.language.taskdef;

import mb.common.style.Styling;
import mb.pie.api.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

public class NoneStyler extends NoneTaskDef<Supplier<?>, Styling> {
    @Inject public NoneStyler(@Named("packageId") String packageId) { super(packageId); }
}
