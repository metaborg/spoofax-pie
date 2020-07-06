package mb.spoofax.core.pie;

import mb.pie.api.Pie;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@LanguageScope
public class DefaultPieProvider implements PieProvider {

    private final Pie languagePie;

    @Inject public DefaultPieProvider(Pie languagePie) {
        this.languagePie = languagePie;
    }

    @Override
    public Pie getPie(@Nullable ResourcePath projectDir) {
        return languagePie;
    }
}
