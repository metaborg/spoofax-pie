package mb.spoofax.eclipse.command;

import mb.common.util.MapView;
import mb.common.util.SerializationUtil;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.ResourceUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class RunCommandHandler extends AbstractHandler {
    public final static String languageIdParameterId = "languageId"; // Used for dynamic loading.
    public final static String dataParameterId = "data";

    private final LoggerFactory loggerFactory;
    private final Logger logger;
    private final EclipseLanguageComponent languageComponent;
    private final PieComponent pieComponent;
    private final PieRunner pieRunner;
    private final ResourceUtil resourceUtil;

    private final MapView<String, CommandDef<?>> commandDefsPerId;

    public RunCommandHandler(EclipseLanguageComponent languageComponent, PieComponent pieComponent) {
        this.loggerFactory = SpoofaxPlugin.getLoggerComponent().getLoggerFactory();
        this.logger = loggerFactory.create(getClass());
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
        final EclipsePlatformComponent component = SpoofaxPlugin.getPlatformComponent();
        this.pieRunner = component.getPieRunner();
        this.resourceUtil = component.getResourceUtil();

        final HashMap<String, CommandDef<?>> transformDefsPerId = new HashMap<>();
        for(CommandDef<?> commandDef : languageComponent.getLanguageInstance().getCommandDefs()) {
            transformDefsPerId.put(commandDef.getId(), commandDef);
        }
        this.commandDefsPerId = new MapView<>(transformDefsPerId);
    }

    @Override public @Nullable Object execute(ExecutionEvent event) throws ExecutionException {
        final @Nullable String dataStr = event.getParameter(dataParameterId);
        if(dataStr == null) {
            throw new ExecutionException("Cannot execute command, no argument for '" + dataParameterId + "' parameter was set");
        }
        final CommandData data = SerializationUtil.deserialize(dataStr, RunCommandHandler.class.getClassLoader());
        final @Nullable CommandDef<?> def = commandDefsPerId.get(data.commandId);
        if(def == null) {
            throw new ExecutionException("Cannot execute command with ID '" + data.commandId + "', command with that ID was not found in language '" + languageComponent.getLanguageInstance().getDisplayName() + "'");
        }
        final CommandRequest<?> request = data.toCommandRequest(def);
        final Pie pie = pieComponent.getPie();
        final RunCommandJob runCommandJob = new RunCommandJob(loggerFactory, languageComponent, pie, pieRunner, data, request);
        final LinkedHashSet<ResourceKey> contextResources = new LinkedHashSet<>();
        for(CommandContext context : data.contexts) {
            collectContextResources(context, contextResources);
        }
        final ArrayList<ISchedulingRule> schedulingRules = new ArrayList<>();
        for(ResourceKey resourceKey : contextResources) {
            try {
                schedulingRules.add(resourceUtil.getEclipseResource(resourceKey));
            } catch(ResourceRuntimeException e) {
                logger.debug("Cannot add '{}' as a scheduling rule for run command job; could not get Eclipse resource corresponding to the resource", e);
            }
        }
        runCommandJob.setRule(new MultiRule(schedulingRules.toArray(new ISchedulingRule[0])));
        runCommandJob.schedule();
        return null;
    }

    private void collectContextResources(CommandContext context, LinkedHashSet<ResourceKey> resources) {
        context.getResourcePathWithKind().ifPresent(p -> resources.add(p.getPath()));
        context.getResourcePathWithKind().ifPresent(p -> {
            final @Nullable ResourcePath parent = p.getPath().getParent();
            // Add parent for ResourcePaths as these paths are usually used to output something into the parent
            // directory, which requires a scheduling rule for the parent.
            if(parent != null) {
                resources.add(parent);
            }
        });
        context.getResourceKey().ifPresent(resources::add);
        for(CommandContext enclosingContext : context.getEnclosings()) {
            collectContextResources(enclosingContext, resources);
        }
    }
}
