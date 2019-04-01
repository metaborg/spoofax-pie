package mb.tiger.cmd;

import mb.fs.java.JavaFileSystem;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.logger.StreamLogger;
import mb.pie.runtime.taskdefs.MapTaskDefs;
import mb.spoofax.cmd.SpoofaxCmd;
import mb.spoofax.core.language.LanguageComponent;
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
        // TODO: extract this into module?
        final MapTaskDefs taskDefs = new MapTaskDefs();
        taskDefs.add(tigerComponent.messagesTaskDef());
        taskDefs.add(tigerComponent.astTaskDef());
        taskDefs.add(tigerComponent.tokenizerTaskDef());
        taskDefs.add(tigerComponent.stylingTaskDef());
        final Pie pie = new PieBuilderImpl()
            .withTaskDefs(taskDefs)
            .withLogger(StreamLogger.verbose())
            .build();
        final SpoofaxCmd cmd = new SpoofaxCmd(tigerComponent, pie);
        cmd.run(args);
    }
}
