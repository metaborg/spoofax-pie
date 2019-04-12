package mb.tiger.cmd;

import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.PieComponent;
import mb.spoofax.cmd.DaggerSpoofaxCmdResourceRegistryComponent;
import mb.spoofax.cmd.SpoofaxCmdResourceRegistryComponent;
import mb.spoofax.cmd.SpoofaxCmd;
import mb.spoofax.core.pie.DaggerSpoofaxTaskDefsComponent;
import mb.spoofax.core.pie.SpoofaxTaskDefsComponent;
import mb.spoofax.core.pie.SpoofaxTaskDefsModule;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.tiger.spoofax.DaggerTigerComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, JSGLR1ParseTableException {
        final SpoofaxCmdResourceRegistryComponent resourceRegistryComponent =
            DaggerSpoofaxCmdResourceRegistryComponent.create();
        final PlatformComponent platformComponent = DaggerPlatformComponent
            .builder()
            .resourceRegistryComponent(resourceRegistryComponent)
            .build();
        final TigerComponent tigerComponent = DaggerTigerComponent
            .builder()
            .platformComponent(platformComponent)
            .tigerModule(TigerModule.fromClassLoaderResources())
            .build();
        final SpoofaxTaskDefsModule taskDefsModule =
            new SpoofaxTaskDefsModule(tigerComponent.getTaskDefs(), platformComponent.getTaskDefs());
        final SpoofaxTaskDefsComponent taskDefsComponent = DaggerSpoofaxTaskDefsComponent
            .builder()
            .spoofaxTaskDefsModule(taskDefsModule)
            .build();
        final PieComponent pieComponent = DaggerPieComponent
            .builder()
            .taskDefsComponent(taskDefsComponent)
            .build();
        final SpoofaxCmd cmd = new SpoofaxCmd(resourceRegistryComponent.getStringResourceRegistry(),
            tigerComponent.getLanguageInstance(), pieComponent.getPie());
        cmd.run(args);
    }
}
