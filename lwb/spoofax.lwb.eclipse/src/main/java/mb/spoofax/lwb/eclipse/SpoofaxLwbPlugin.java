package mb.spoofax.lwb.eclipse;

import mb.cfg.eclipse.CfgLanguage;
import mb.esv.eclipse.EsvLanguage;
import mb.libspoofax2.eclipse.LibSpoofax2Language;
import mb.libstatix.eclipse.LibStatixLanguage;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.sdf3.eclipse.Sdf3Language;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;
import mb.spoofax.lwb.dynamicloading.DynamicLoader;
import mb.statix.eclipse.StatixLanguage;
import mb.str.eclipse.StrategoLanguage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SpoofaxLwbPlugin extends AbstractUIPlugin implements IStartup {
    public static final String id = "spoofax.lwb.eclipse";

    private static @Nullable SpoofaxLwbPlugin plugin;
    private static @Nullable Spoofax3Compiler spoofax3Compiler;
    private static @Nullable DynamicLoader dynamicLoader;

    public static SpoofaxLwbPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException("Cannot access SpoofaxLwbPlugin instance; plugin has not been started yet, or has been stopped");
        }
        return plugin;
    }

    public static Spoofax3Compiler getSpoofax3Compiler() {
        if(spoofax3Compiler == null) {
            throw new RuntimeException("Cannot access Spoofax3Compiler instance; plugin has not been started yet, or has been stopped");
        }
        return spoofax3Compiler;
    }

    public static DynamicLoader getDynamicLoader() {
        if(dynamicLoader == null) {
            throw new RuntimeException("Cannot access DynamicLoader instance; plugin has not been started yet, or has been stopped");
        }
        return dynamicLoader;
    }


    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        final String lwbGroup = "mb.spoofax.lwb";
        spoofax3Compiler = new Spoofax3Compiler(
            SpoofaxPlugin.getLoggerComponent(),
            SpoofaxPlugin.getResourceServiceComponentOfGroup(lwbGroup),
            SpoofaxPlugin.getPlatformComponent(),

            SpoofaxPlugin.getPieComponentOfGroup(lwbGroup).createChildModule(),

            CfgLanguage.getInstance().getComponent(),
            Sdf3Language.getInstance().getComponent(),
            StrategoLanguage.getInstance().getComponent(),
            EsvLanguage.getInstance().getComponent(),
            StatixLanguage.getInstance().getComponent(),
            LibSpoofax2Language.getInstance().getComponent(),
            LibSpoofax2Language.getInstance().getResourcesComponent(),
            LibStatixLanguage.getInstance().getComponent(),
            LibStatixLanguage.getInstance().getResourcesComponent()
        );
        dynamicLoader = new DynamicLoader(spoofax3Compiler, () -> new RootPieModule(PieBuilderImpl::new));
    }

    @Override public void earlyStartup() {
        // Early startup forces this plugin to be started when Eclipse starts.
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(dynamicLoader != null) {
            dynamicLoader.close();
            dynamicLoader = null;
        }
        spoofax3Compiler = null;
        plugin = null;
    }

}
