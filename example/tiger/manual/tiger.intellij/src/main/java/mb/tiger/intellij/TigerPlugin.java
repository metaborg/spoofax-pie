package mb.tiger.intellij;

import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.intellij.SpoofaxPlugin;
import mb.tego.strategies.DaggerTegoComponent;
import mb.tego.strategies.TegoComponent;
import mb.tiger.spoofax.DaggerTigerResourcesComponent;
import mb.tiger.spoofax.TigerResourcesComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TigerPlugin {
    private static @Nullable TigerResourcesComponent resourcesComponent;
    private static @Nullable ResourceServiceComponent resourceServiceComponent;
    private static @Nullable TigerIntellijComponent component;
    private static @Nullable PieComponent pieComponent;
    private static @Nullable TegoComponent tegoComponent;

    public static TigerResourcesComponent getResourcesComponent() {
        if(resourcesComponent == null) {
            throw new RuntimeException("Cannot access TigerResourcesComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return resourcesComponent;
    }

    public static ResourceServiceComponent getResourceServiceComponent() {
        if(resourceServiceComponent == null) {
            throw new RuntimeException("Cannot access ResourceServiceComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return resourceServiceComponent;
    }

    public static TigerIntellijComponent getComponent() {
        if(component == null) {
            throw new RuntimeException("Cannot access TigerComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    public static PieComponent getPieComponent() {
        if(pieComponent == null) {
            throw new RuntimeException("Cannot access PieComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return pieComponent;
    }

    public static TegoComponent getTegoComponent() {
        if(tegoComponent == null) {
            throw new RuntimeException("Cannot access TegoComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return tegoComponent;
    }

    public static void init() {
        resourcesComponent = DaggerTigerResourcesComponent.create();
        resourceServiceComponent = DaggerResourceServiceComponent.builder()
            .resourceServiceModule(SpoofaxPlugin.getResourceServiceComponent().createChildModule().addRegistriesFrom(resourcesComponent))
            .loggerComponent(SpoofaxPlugin.getLoggerComponent())
            .build();
        tegoComponent = DaggerTegoComponent.builder()
            .loggerComponent(SpoofaxPlugin.getLoggerComponent())
            .build();
        component = DaggerTigerIntellijComponent.builder()
            .intellijLoggerComponent(SpoofaxPlugin.getLoggerComponent())
            .tigerResourcesComponent(resourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .intellijPlatformComponent(SpoofaxPlugin.getPlatformComponent())
            .tegoComponent(tegoComponent)
            .build();
        pieComponent = DaggerRootPieComponent.builder()
            .rootPieModule(new RootPieModule(PieBuilderImpl::new, component))
            .loggerComponent(SpoofaxPlugin.getLoggerComponent())
            .resourceServiceComponent(resourceServiceComponent)
            .build();
    }
}
