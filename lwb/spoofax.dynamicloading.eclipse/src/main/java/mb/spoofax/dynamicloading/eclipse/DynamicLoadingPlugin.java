package mb.spoofax.dynamicloading.eclipse;

import mb.esv.eclipse.EsvLanguage;
import mb.libspoofax2.eclipse.LibSpoofax2Language;
import mb.libstatix.eclipse.LibStatixLanguage;
import mb.pie.runtime.PieBuilderImpl;
import mb.sdf3.eclipse.Sdf3Language;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3Compiler;
import mb.spoofax.compiler.spoofax3.standalone.dagger.Spoofax3CompilerStandalone;
import mb.spoofax.dynamicloading.DynamicLoader;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.statix.eclipse.StatixLanguage;
import mb.str.eclipse.StrategoLanguage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class DynamicLoadingPlugin extends AbstractUIPlugin {
    public static final String id = "spoofax.dynamicloading.eclipse";

    private static @Nullable DynamicLoadingPlugin plugin;
    private static @Nullable Spoofax3CompilerStandalone spoofax3CompilerStandalone;
    private static @Nullable DynamicLoader dynamicLoader;

    public static DynamicLoadingPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException("Cannot access DynamicLoadingPlugin instance; it has not been started yet, or has been stopped");
        }
        return plugin;
    }


    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        final Spoofax3Compiler spoofax3Compiler = new Spoofax3Compiler(
            SpoofaxPlugin.getResourceServiceComponent(),
            SpoofaxPlugin.getPlatformComponent(),
            PieBuilderImpl::new,
            Sdf3Language.getInstance().getComponent(),
            StrategoLanguage.getInstance().getComponent(),
            EsvLanguage.getInstance().getComponent(),
            StatixLanguage.getInstance().getComponent(),
            LibSpoofax2Language.getInstance().getComponent(),
            LibSpoofax2Language.getInstance().getResourcesComponent(),
            LibStatixLanguage.getInstance().getComponent(),
            LibStatixLanguage.getInstance().getResourcesComponent()
        );
        spoofax3CompilerStandalone = new Spoofax3CompilerStandalone(spoofax3Compiler);
        dynamicLoader = new DynamicLoader(spoofax3CompilerStandalone);
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(dynamicLoader != null) {
            dynamicLoader.close();
            dynamicLoader = null;
        }
        spoofax3CompilerStandalone = null;
        plugin = null;
    }
}
