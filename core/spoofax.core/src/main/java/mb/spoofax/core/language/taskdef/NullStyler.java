package mb.spoofax.core.language.taskdef;

import mb.common.style.Styling;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@LanguageScope
public class NullStyler extends NullTaskDef<Supplier<?>, @Nullable Styling> {

    @Inject public NullStyler() {}

}
