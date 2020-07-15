package mb.statix.multilang.eclipse;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.eclipse.job.LockRule;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.MultiLangModule;
import mb.statix.multilang.MultiLangScope;

@MultiLangScope
@Component(
    modules = {MultiLangModule.class, MultiLangEclipseModule.class},
    dependencies = PlatformComponent.class
)
public interface MultiLangEclipseComponent extends MultiLangComponent {
    @MultiLang LockRule startUpLockRule();
}
