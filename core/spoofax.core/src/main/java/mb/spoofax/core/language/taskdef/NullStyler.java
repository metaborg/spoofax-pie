package mb.spoofax.core.language.taskdef;

import mb.common.style.Styling;
import mb.pie.api.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class NullStyler extends NullTaskDef<Supplier<?>, @Nullable Styling> {
    @Inject public NullStyler() {}
}
