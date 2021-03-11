package mb.str.task;

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
import mb.str.StrategoScope;
import mb.str.config.StrategoAnalyzeConfig;
import mb.stratego.build.strincr.Frontends;
import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.StrIncrAnalysis;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.StrategoGradualSetting;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

@StrategoScope
public class StrategoAnalyze implements TaskDef<StrategoAnalyze.Input, KeyedMessages> {
    public static class Input implements Serializable {
        public final StrategoAnalyzeConfig config;
        public final ArrayList<STask<?>> originTasks;

        public Input(
            StrategoAnalyzeConfig config,
            ArrayList<STask<?>> originTasks
        ) {
            this.config = config;
            this.originTasks = originTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return config.equals(input.config) && originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(config, originTasks);
        }

        @Override public String toString() {
            return "Input{" +
                "config=" + config +
                ", originTasks=" + originTasks +
                '}';
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

    @Override public KeyedMessages exec(ExecContext context, Input input) {
        //noinspection ConstantConditions
        final Result<Frontends.Output, ?> result = Result.ofOkOrCatching(() -> context.require(analysis, new Frontends.Input(
            input.config.mainFile,
            input.config.includeDirs.asUnmodifiable(),
            input.config.builtinLibs.asUnmodifiable(),
            input.originTasks,
            input.config.projectDir,
            StrategoGradualSetting.NONE
        )));
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        result.ifElse(output -> {
            for(Message<?> message : output.messages) {
                final @Nullable Region region = TermTracer.getRegion(message.locationTerm);
                final ResourceKey resourceKey = resourceService.getResourceKey(ResourceKeyString.parse(message.moduleFilePath));
                final Severity severity = convertSeverity(message.severity);
                messagesBuilder.addMessage(message.getMessage(), severity, resourceKey, region);
            }
        }, ex -> messagesBuilder.addMessage("Stratego analysis failed unexpectedly", ex, Severity.Error, input.config.mainFile));
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
