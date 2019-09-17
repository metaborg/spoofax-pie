package mb.spoofax.cli;

import mb.pie.api.PieSession;
import mb.resource.ResourceService;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.cli.CliCommandItemVisitor;
import mb.spoofax.core.language.cli.CliParam;
import mb.spoofax.core.language.cli.CliParamDef;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.arg.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Model.*;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Stack;

public class SpoofaxCli {
    private final ResourceService resourceService;
    private final ArgConverters argConverters;

    @Inject
    public SpoofaxCli(ResourceService resourceService, ArgConverters argConverters) {
        this.resourceService = resourceService;
        this.argConverters = argConverters;
    }

    public int run(String[] args, LanguageComponent languageComponent) {
        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        try(final PieSession pieSession = languageComponent.newPieSession()) {
            final CommandSpec[] rootCommandSpec = new CommandSpec[1];
            final Stack<CommandSpec> commandSpecStack = new Stack<>();
            languageInstance.getRootCliCommandItem().accept(new CliCommandItemVisitor() {
                @Override public void commandListPush(String name, @Nullable String description) {
                    final DelegateRunnable delegateRunnable = new DelegateRunnable();
                    final CommandSpec commandSpec = CommandSpec.wrapWithoutInspection(delegateRunnable);
                    commandSpec.mixinStandardHelpOptions(true);
                    commandSpec.name(name);
                    if(description != null) {
                        commandSpec.usageMessage().description(description);
                    }
                    delegateRunnable.setRunnable(() -> {
                        new CommandLine(commandSpec).usage(System.out);
                    });

                    if(!commandSpecStack.isEmpty()) {
                        commandSpecStack.peek().addSubcommand(name, commandSpec);
                    } else {
                        rootCommandSpec[0] = commandSpec;
                    }
                    commandSpecStack.push(commandSpec);
                }

                @Override public void commandListPop() {
                    commandSpecStack.pop();
                }

                @Override
                public void command(CommandDef<?> def, String commandName, CliParamDef cliParamDef, @Nullable String commandDescription) {
                    final CommandRunner<?> commandRunner = new CommandRunner<>(resourceService, pieSession, def, argConverters);
                    final CommandSpec commandSpec = CommandSpec.wrapWithoutInspection(commandRunner);
                    commandSpec.mixinStandardHelpOptions(true);
                    commandSpec.name(commandName);
                    final ParamDef paramDef = def.getParamDef();
                    for(CliParam cliParam : cliParamDef.params) {
                        commandSpec.add(cliParam.caseOf()
                            .option((paramId, names, negatable, label, description, converter) -> {
                                final @Nullable Param param = paramDef.params.get(paramId);
                                if(param == null) {
                                    throw new RuntimeException("Could not create command '" + commandName + "', no parameter was found for ID '" + paramId + "'");
                                }
                                final OptionSpec.Builder builder = OptionSpec.builder(names.toArray(new String[0]));
                                builder.type(param.getType());
                                builder.required(param.isRequired());
                                builder.negatable(negatable);
                                final @Nullable Serializable provided = RawArgFromProviders.get(param, new CommandContext());
                                if(provided != null) {
                                    builder.defaultValue(provided.toString());
                                    builder.showDefaultValue(CommandLine.Help.Visibility.ALWAYS);
                                }
                                // noinspection ConstantConditions (label can really be null)
                                if(label != null) {
                                    builder.paramLabel(label);
                                }
                                // noinspection ConstantConditions (label can really be null)
                                if(description != null) {
                                    builder.description(description);
                                }
                                // noinspection ConstantConditions (converter can really be null)
                                if(converter != null) {
                                    builder.converters(new TypeConverter<>(converter));
                                }
                                builder.setter(new ISetter() {
                                    @Override
                                    public <T> @Nullable T set(@Nullable T value) throws IllegalArgumentException {
                                        commandRunner.set(paramId, value);
                                        return null;
                                    }
                                });
                                return (ArgSpec) builder.build();
                            })
                            .positional((paramId, index, label, description, converter) -> {
                                final @Nullable Param param = paramDef.params.get(paramId);
                                if(param == null) {
                                    throw new RuntimeException("Could not create command '" + commandName + "', no parameter was found for ID '" + paramId + "'");
                                }
                                final PositionalParamSpec.Builder builder = PositionalParamSpec.builder();
                                builder.index(Integer.toString(index));
                                builder.type(param.getType());
                                builder.required(param.isRequired());
                                final @Nullable Serializable provided = RawArgFromProviders.get(param, new CommandContext());
                                if(provided != null) {
                                    builder.defaultValue(provided.toString());
                                    builder.showDefaultValue(CommandLine.Help.Visibility.ALWAYS);
                                }
                                // noinspection ConstantConditions (label can really be null)
                                if(label != null) {
                                    builder.paramLabel(label);
                                }
                                // noinspection ConstantConditions (label can really be null)
                                if(description != null) {
                                    builder.description(description);
                                }
                                // noinspection ConstantConditions (converter can really be null)
                                if(converter != null) {
                                    builder.converters(new TypeConverter<>(converter));
                                }
                                builder.setter(new ISetter() {
                                    @Override
                                    public <T> @Nullable T set(@Nullable T value) throws IllegalArgumentException {
                                        commandRunner.set(paramId, value);
                                        return null;
                                    }
                                });
                                return (ArgSpec) builder.build();
                            }));
                    }
                    if(commandDescription != null) {
                        commandSpec.usageMessage().description(commandDescription);
                    }
                    if(!commandSpecStack.isEmpty()) {
                        commandSpecStack.peek().addSubcommand(commandName, commandSpec);
                    } else {
                        rootCommandSpec[0] = commandSpec;
                    }
                }
            });
            final CommandLine commandLine = new CommandLine(rootCommandSpec[0]);
            for(ArgConverter<?> converter : argConverters.allConverters.values()) {
                registerConverter(commandLine, new TypeConverter<>(converter));
            }
            return commandLine.execute(args);
        }
    }

    private static <T extends Serializable> void registerConverter(CommandLine commandLine, TypeConverter<T> converter) {
        commandLine.registerConverter(converter.getOutputClass(), converter);
    }
}
