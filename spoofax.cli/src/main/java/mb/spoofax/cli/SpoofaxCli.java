package mb.spoofax.cli;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.cli.CliCommandBinding;
import mb.spoofax.core.language.cli.CliParam;
import mb.spoofax.core.language.cli.CliParams;
import mb.spoofax.core.language.command.arg.ArgConverter;
import mb.spoofax.core.language.command.arg.DefaultArgConverters;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Model.*;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;

public class SpoofaxCli {
    private final DefaultArgConverters defaultArgConverters;

    @Inject
    public SpoofaxCli(DefaultArgConverters defaultArgConverters) {
        this.defaultArgConverters = defaultArgConverters;
    }

    public int run(String[] args, LanguageComponent languageComponent) {
        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();

        final DelegateRunnable mainRunnable = new DelegateRunnable();
        final CommandSpec mainCommandSpec = CommandSpec.wrapWithoutInspection(mainRunnable);
        mainCommandSpec.mixinStandardHelpOptions(true);
        mainCommandSpec.name(languageInstance.getDisplayName());

        for(CliCommandBinding binding : languageInstance.getCliCommandBindings()) {
            final CommandRunner<?> commandRunner = new CommandRunner<>(languageComponent, binding.getDef(), defaultArgConverters);
            final CommandSpec commandSpec = CommandSpec.wrapWithoutInspection(commandRunner);
            commandSpec.mixinStandardHelpOptions(true);
            commandSpec.name(binding.getName());
            final ParamDef paramDef = binding.getDef().getParamDef();
            for(Map.Entry<String, CliParam> entry : binding.getParamDef().params.entrySet()) {
                final String paramId = entry.getKey();
                final CliParam cliParam = entry.getValue();
                final @Nullable Param param = paramDef.params.get(paramId);
                if(param == null) {
                    throw new RuntimeException("Could not create command '" + binding.getName() + "', no parameter was found for ID '" + paramId + "'");
                }
                commandSpec.add(CliParams.caseOf(cliParam)
                    .option((_paramId, names, label, description) -> {
                        final OptionSpec.Builder builder = OptionSpec.builder(names.toArray(new String[0]));
                        builder.type(param.getType());
                        builder.required(param.isRequired());
                        // TODO: providers/default values
                        // noinspection ConstantConditions (label can really be null)
                        if(label != null) {
                            builder.paramLabel(label);
                        }
                        // noinspection ConstantConditions (label can really be null)
                        if(description != null) {
                            builder.description(description);
                        }
                        builder.setter(new ISetter() {
                            @Override public <T> @Nullable T set(@Nullable T value) throws IllegalArgumentException {
                                commandRunner.set(paramId, value);
                                return null;
                            }
                        });
                        return (ArgSpec) builder.build();
                    })
                    .positional((_paramId, index, label, description) -> {
                        final PositionalParamSpec.Builder builder = PositionalParamSpec.builder();
                        builder.index(Integer.toString(index));
                        builder.type(param.getType());
                        builder.required(param.isRequired());
                        // TODO: providers/default values
                        // noinspection ConstantConditions (label can really be null)
                        if(label != null) {
                            builder.paramLabel(label);
                        }
                        // noinspection ConstantConditions (label can really be null)
                        if(description != null) {
                            builder.description(description);
                        }
                        builder.setter(new ISetter() {
                            @Override public <T> @Nullable T set(@Nullable T value) throws IllegalArgumentException {
                                commandRunner.set(paramId, value);
                                return null;
                            }
                        });
                        return (ArgSpec) builder.build();
                    }));
            }
            commandSpec.usageMessage().description(binding.getDescription());
            mainCommandSpec.addSubcommand(binding.getName(), commandSpec);
        }

        final CommandLine commandLine = new CommandLine(mainCommandSpec);
        for(ArgConverter<?> converter : defaultArgConverters.allConverters.values()) {
            registerConverter(commandLine, new TypeConverter<>(converter));
        }
        mainRunnable.setRunnable(() -> {
            commandLine.usage(System.out);
        });
        return commandLine.execute(args);
    }

    private static <T extends Serializable> void registerConverter(CommandLine commandLine, TypeConverter<T> converter) {
        commandLine.registerConverter(converter.getOutputClass(), converter);
    }
}
