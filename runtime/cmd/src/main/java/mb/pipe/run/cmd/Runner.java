package mb.pipe.run.cmd;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;
import com.sun.nio.file.SensitivityWatchEventModifier;

import build.pluto.builder.BuildManager;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.database.XodusDatabase;
import build.pluto.util.LogReporting;
import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.model.message.IMsg;
import mb.pipe.run.core.model.parse.IToken;
import mb.pipe.run.core.model.style.IStyling;
import mb.pipe.run.core.model.style.ITokenStyle;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.core.vfs.VFSResource;
import mb.pipe.run.pluto.generated.filePipeline;
import mb.pipe.run.pluto.generated.filePipeline.Output;

@SuppressWarnings("restriction")
public class Runner {
    private static final ILogger logger = LoggerUtils.logger(Runner.class);

    private final IResourceService resourceService;


    @Inject public Runner(IResourceService resourceService) {
        this.resourceService = resourceService;
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

        final FileObject currentDir = resourceService.resolve(".");
        final IContext context = new Context(new VFSResource(currentDir));

        clean(context, arguments);
        build(context, arguments);

        if(!arguments.continuous) {
            return 0;
        }

        final java.nio.file.Path path = FileSystems.getDefault().getPath(currentDir.getName().getPath());
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
                    final boolean relevantChange =
                        pathStr.endsWith(".sdf3") || pathStr.endsWith(".esv") || pathStr.endsWith(".min");
                    if(relevantChange) {
                        logger.info("Relevant change: {}", pathStr);
                    }
                    build |= relevantChange;
                }
                if(build) {
                    build(context, arguments);
                }
                key.reset();
            }
        }

        return 0;
    }


    private static void build(IContext context, Arguments arguments) throws Throwable {
        final IResource file = new VFSResource(arguments.file);
        final BuildRequest<?, Output, ?, ?> buildRequest =
            filePipeline.request(new filePipeline.Input(context, null, file, context));

        try(final BuildManager buildManager =
            new BuildManager(new LogReporting(), XodusDatabase.createFileDatabase("pipeline-experiment"))) {
            if(arguments.clean) {
                buildManager.resetDynamicAnalysis();
            }

            final Output output = buildManager.requireInitially(buildRequest).getBuildResult();
            final IResource resource = (IResource) output.getPipeVal().get(0);
            final String text = (String) output.getPipeVal().get(1);
            final @Nullable IStrategoTerm ast = (IStrategoTerm) output.getPipeVal().get(2);
            final @Nullable Collection<IToken> tokenStream = (Collection<IToken>) output.getPipeVal().get(3);
            final Collection<IMsg> messages = (Collection<IMsg>) output.getPipeVal().get(4);
            final @Nullable IStyling styling = (IStyling) output.getPipeVal().get(5);
            if(ast == null || tokenStream == null) {
                logger.info("Parsing failed, messages: ");
                for(IMsg msg : messages) {
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
                for(IToken token : tokenStream) {
                    logger.info("{} ({}): '{}'", token, token.type(), token.textPart(text));
                }
                if(recovered) {
                    logger.info("Messages: ");
                    for(IMsg msg : messages) {
                        logger.info(msg.text());
                    }
                }

                if(styling == null) {
                    logger.info("Styling failed");
                } else {
                    logger.info("Styling: ");
                    for(ITokenStyle tokenStyle : styling.stylePerToken()) {
                        logger.info("Token {} - Style {}", tokenStyle.token(), tokenStyle.style());
                    }
                }
            }
        } catch(MetaborgException | IOException e) {
            logger.error("Build failed", e);
        }
    }

    private static void clean(IContext context, Arguments arguments) throws Throwable {
        if(!arguments.clean) {
            return;
        }

        context.persistentDir().fileObject().delete(new AllFileSelector());
        try(final BuildManager buildManager =
            new BuildManager(new LogReporting(), XodusDatabase.createFileDatabase("pipeline-experiment"))) {
            buildManager.resetDynamicAnalysis();
        }
    }

    private static void registerWatcher(java.nio.file.Path path, WatchService watchService) throws IOException {
        path.register(
            watchService, new WatchEvent.Kind[] { StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE },
            SensitivityWatchEventModifier.HIGH);
    }
}
