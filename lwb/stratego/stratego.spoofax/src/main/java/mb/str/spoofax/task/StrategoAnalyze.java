package mb.str.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.Analysis;
import mb.stratego.build.strincr.Message;
import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.StrIncrAnalysis;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class StrategoAnalyze implements TaskDef<StrategoAnalyze.Args, KeyedMessages> {
    public static class Args implements Serializable {
        public final ResourcePath projectDir;
        public final ResourcePath mainFile;
        public final ArrayList<ResourcePath> includeDirs;
        public final ArrayList<String> builtinLibs;
        public final ArrayList<STask> originTasks;

        public Args(
            ResourcePath projectDir,
            ResourcePath mainFile,
            ArrayList<ResourcePath> includeDirs,
            ArrayList<String> builtinLibs,
            ArrayList<STask> originTasks
        ) {
            this.projectDir = projectDir;
            this.mainFile = mainFile;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.originTasks = originTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return projectDir.equals(args.projectDir) &&
                mainFile.equals(args.mainFile) &&
                includeDirs.equals(args.includeDirs) &&
                builtinLibs.equals(args.builtinLibs) &&
                originTasks.equals(args.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(projectDir, mainFile, includeDirs, builtinLibs, originTasks);
        }
    }


    private final ResourceService resourceService;
    private final StrIncrAnalysis analysis;

    @Inject public StrategoAnalyze(ResourceService resourceService, StrIncrAnalysis analysis) {
        this.resourceService = resourceService;
        this.analysis = analysis;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Args args) {
        //noinspection ConstantConditions
        final Result<Analysis.Output, ?> result = Result.ofOkOrCatching(() -> context.require(analysis, new Analysis.Input(
            args.mainFile, args.includeDirs, args.builtinLibs, args.originTasks, args.projectDir
        )));
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        result.ifElse(output -> {
            for(Message<?> message : output.messages) {
                final @Nullable Region region = TermTracer.getRegion(message.locationTerm);
                final ResourceKey resourceKey = resourceService.getResourceKey(ResourceKeyString.parse(message.moduleFilePath));
                final Severity severity = convertSeverity(message.severity);
                messagesBuilder.addMessage(message.getMessage(), severity, resourceKey, region);
            }
        }, ex -> messagesBuilder.addMessage("Stratego analysis failed unexpectedly", ex, Severity.Error, args.mainFile));
        return messagesBuilder.build();
    }

    private static Severity convertSeverity(MessageSeverity severity) {
        switch(severity) {
            case NOTE:
                return Severity.Info;
            case WARNING:
                return Severity.Warning;
            case ERROR:
                return Severity.Error;
        }
        return Severity.Error;
    }
}
