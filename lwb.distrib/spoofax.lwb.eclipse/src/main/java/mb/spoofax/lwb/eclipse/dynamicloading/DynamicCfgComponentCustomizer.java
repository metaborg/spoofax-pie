package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.cfg.CfgCustomizerModule;
import mb.cfg.DaggerCfgResourcesComponent;
import mb.cfg.eclipse.CfgComponentCustomizer;
import mb.cfg.eclipse.DaggerCfgEclipseComponent;

public class DynamicCfgComponentCustomizer implements CfgComponentCustomizer {
    @Override public void customize(DaggerCfgResourcesComponent.Builder builder) {

    }

    @Override public void customize(DaggerCfgEclipseComponent.Builder builder) {
        builder.cfgCustomizerModule(new CfgCustomizerModule(new DynamicCompileLanguageDefinitionInputCustomizer()));
    }
}
