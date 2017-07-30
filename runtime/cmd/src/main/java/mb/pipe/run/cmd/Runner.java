package mb.pipe.run.cmd;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;

import mb.ceres.BuildApp;
import mb.ceres.BuildException;
import mb.ceres.BuildManager;
import mb.pipe.run.ceres.CeresSrv;
import mb.vfs.path.PPath;
import mb.vfs.path.PathSrv;

@SuppressWarnings("restriction")
public class Runner {
    private static final ILogger logger = LoggerUtils.logger(Runner.class);

    private final PathSrv pathSrv;
    private final CeresSrv ceresSrv;


    @Inject public Runner(PathSrv resourceService, CeresSrv ceresSrv) {
        this.pathSrv = resourceService;
        this.ceresSrv = ceresSrv;
    }


    public int run(String[] args) throws Throwable {
        final Arguments arguments = new Arguments();
        final JCommander jc = new JCommander(arguments);

        try {
            jc.parse(args);
        } catch(ParameterException e) {
            System.err.println(e.getMessage());
            final StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            System.err.println(sb.toString());
            return -1;
        }

        if(arguments.help) {
            final StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            System.err.println(sb.toString());
            return 0;
        }

        if(arguments.exit) {
            System.err.println("Exitting immediately for testing purposes");
            return 0;
        }

        final PPath currentDir = pathSrv.resolveLocal(".");
        final BuildManager buildManager = ceresSrv.get(currentDir);

        build(arguments, buildManager);

        if(!arguments.continuous) {
            return 0;
        }

        final java.nio.file.Path path = currentDir.getJavaPath();
        try(final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            registerWatcher(path, watchService);
            Files.walkFileTree(path, new SimpleFileVisitor<java.nio.file.Path>() {
                @Override public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs)
                    throws IOException {
                    registerWatcher(dir, watchService);
                    return FileVisitResult.CONTINUE;
                }
            });

            final AtomicBoolean stop = new AtomicBoolean(false);
            while(!stop.get()) {
                final WatchKey key = watchService.poll(25, TimeUnit.MILLISECONDS);
                if(key == null) {
                    continue;
                }
                boolean build = false;
                for(WatchEvent<?> event : key.pollEvents()) {
                    final java.nio.file.Path changedPath = (java.nio.file.Path) event.context();
                    final String pathStr = changedPath.toString();
                    // final boolean relevantChange =
                    // pathStr.endsWith(".sdf3") || pathStr.endsWith(".esv") || pathStr.endsWith(".min");
                    // if(relevantChange) {
                    // logger.info("Relevant change: {}", pathStr);
                    // }
                    build |= true;
                }
                if(build) {
                    build(arguments, buildManager);
                }
                key.reset();
            }
        }

        return 0;
    }


    private static void build(Arguments arguments, BuildManager buildManager) throws BuildException {
        if(arguments.drop) {
            buildManager.dropCache();
            buildManager.dropStore();
        }
        final BuildApp<ArrayList<String>, Serializable> app = new BuildApp<>(arguments.function, arguments.arguments);
        final Serializable result;
        try {
            result = buildManager.build(app);
        } catch(BuildException e) {
            logger.error("Build failed", e);
            return;
        }

        logger.info("Build succeeded: {}", result);
    }

    private static void registerWatcher(java.nio.file.Path path, WatchService watchService) throws IOException {
        path.register(
            watchService, new WatchEvent.Kind[] { StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE },
            com.sun.nio.file.SensitivityWatchEventModifier.HIGH);
    }
}
