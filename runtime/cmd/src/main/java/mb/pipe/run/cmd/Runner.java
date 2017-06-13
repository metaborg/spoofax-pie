package mb.pipe.run.cmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;

import mb.ceres.BuildException;
import mb.ceres.BuildManager;
import mb.ceres.BuildManagerFactory;
import mb.ceres.impl.BuildCache;
import mb.ceres.impl.MapBuildCache;
import mb.ceres.internal.BuildStore;
import mb.ceres.internal.LMDBBuildStore;
import mb.pipe.run.ceres.generated.processFile;
import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.ContextImpl;
import mb.pipe.run.core.model.message.Msg;
import mb.pipe.run.core.model.parse.Token;
import mb.pipe.run.core.model.style.Styling;
import mb.pipe.run.core.model.style.TokenStyle;
import mb.pipe.run.core.path.PPath;
import mb.pipe.run.core.path.PathSrv;

@SuppressWarnings("restriction")
public class Runner {
    private static final ILogger logger = LoggerUtils.logger(Runner.class);

    private final PathSrv pathSrv;
    private final BuildManagerFactory buildManagerFactory;


    @Inject public Runner(PathSrv resourceService, BuildManagerFactory buildManagerFactory) {
        this.pathSrv = resourceService;
        this.buildManagerFactory = buildManagerFactory;
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
        final Context context = new ContextImpl(currentDir);

        final BuildCache cache = new MapBuildCache();
        try(final BuildStore store = new LMDBBuildStore(new File("target/ceres"), 1024 * 1024 * 128, 8)) {
            clean(arguments, store, cache);
            build(context, arguments, store, cache);

            if(!arguments.continuous) {
                return 0;
            }

            final java.nio.file.Path path = currentDir.getJavaPath();
            try(final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                registerWatcher(path, watchService);
                Files.walkFileTree(path, new SimpleFileVisitor<java.nio.file.Path>() {
                    @Override public FileVisitResult preVisitDirectory(java.nio.file.Path dir,
                        BasicFileAttributes attrs) throws IOException {
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
                        final boolean relevantChange =
                            pathStr.endsWith(".sdf3") || pathStr.endsWith(".esv") || pathStr.endsWith(".min");
                        if(relevantChange) {
                            logger.info("Relevant change: {}", pathStr);
                        }
                        build |= relevantChange;
                    }
                    if(build) {
                        build(context, arguments, store, cache);
                    }
                    key.reset();
                }
            }

            return 0;
        }
    }


    private void build(Context context, Arguments arguments, BuildStore store, BuildCache cache) {
        final PPath file = pathSrv.resolve(arguments.file);

        final BuildManager buildManager = buildManagerFactory.create(store, cache);

        final processFile.Output result;
        try {
            result = buildManager.build(processFile.class, new processFile.Input(file, context));
        } catch(BuildException e) {
            logger.error("Build failed", e);
            return;
        }

        final String text = result.component2();
        final @Nullable IStrategoTerm ast = result.component3();
        final @Nullable Collection<Token> tokenStream = result.component4();
        final List<Msg> messages = result.component5();
        final @Nullable Styling styling = result.component6();

        if(ast == null || tokenStream == null) {
            logger.info("Parsing failed, messages: ");
            for(Msg msg : messages) {
                logger.info(msg.text());
            }
        } else {
            final boolean recovered = !messages.isEmpty();
            if(!recovered) {
                logger.info("Parsing successful");
            } else {
                logger.info("Parsed with errors, AST and token stream were recovered:");
            }
            logger.info("AST: {}", ast);
            logger.info("Token stream: ");
            for(Token token : tokenStream) {
                logger.info("{} ({}): '{}'", token, token.type(), token.textPart(text));
            }
            if(recovered) {
                logger.info("Messages: ");
                for(Msg msg : messages) {
                    logger.info(msg.text());
                }
            }

            if(styling == null) {
                logger.info("Styling failed");
            } else {
                logger.info("Styling: ");
                for(TokenStyle tokenStyle : styling.stylePerToken()) {
                    logger.info("Token {} - Style {}", tokenStyle.token(), tokenStyle.style());
                }
            }
        }
    }

    private static void clean(Arguments arguments, BuildStore store, BuildCache cache) throws Throwable {
        if(!arguments.clean) {
            return;
        }

        store.reset();
        cache.clear();
    }

    private static void registerWatcher(java.nio.file.Path path, WatchService watchService) throws IOException {
        path.register(
            watchService, new WatchEvent.Kind[] { StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE },
            com.sun.nio.file.SensitivityWatchEventModifier.HIGH);
    }
}
