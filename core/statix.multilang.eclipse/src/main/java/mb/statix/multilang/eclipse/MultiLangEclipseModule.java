package mb.statix.multilang.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.eclipse.job.LockRule;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangScope;

@Module
public class MultiLangEclipseModule {
    @Provides @MultiLang @MultiLangScope
    static LockRule provideMultilangStartupLockRule() {
        return new LockRule("Multilang startup lockrule");
    }
}
