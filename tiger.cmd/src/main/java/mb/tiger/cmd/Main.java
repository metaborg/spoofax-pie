package mb.tiger.cmd;

import mb.fs.java.JavaFileSystem;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.DaggerResourceSystemsComponent;
import mb.pie.dagger.PieComponent;
import mb.spoofax.cmd.SpoofaxCmd;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.FileSystemResourceServiceModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.tiger.spoofax.DaggerTigerComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, JSGLR1ParseTableException {
        final PlatformComponent platformComponent = DaggerPlatformComponent
            .builder()
            .fileSystemResourceServiceModule(new FileSystemResourceServiceModule(JavaFileSystem.instance))
            .build();
        final TigerComponent tigerComponent = DaggerTigerComponent
            .builder()
            .platformComponent(platformComponent)
            .tigerModule(TigerModule.fromClassLoaderResources())
            .build();
        final PieComponent pieComponent = DaggerPieComponent
            .builder()
            .taskDefsComponent(tigerComponent)
            .resourceSystemsComponent(DaggerResourceSystemsComponent.create())
            .build();
        final SpoofaxCmd cmd = new SpoofaxCmd(tigerComponent, pieComponent.getPie());
        cmd.run(args);
    }
}
