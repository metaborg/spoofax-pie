package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Mustache;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.cli.CliCommandRepr;
import mb.spoofax.compiler.command.AutoCommandDefRepr;
import mb.spoofax.compiler.command.CommandDefRepr;
import mb.spoofax.compiler.menu.MenuItemRepr;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.NamedTypeInfo;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.compiler.util.UniqueNamer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class AdapterProject {
    private final TemplateWriter buildGradleTemplate;
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter checkTaskDefTemplate;
    private final TemplateWriter componentTemplate;
    private final TemplateWriter moduleTemplate;
    private final TemplateWriter instanceTemplate;
    private final TemplateWriter commandDefTemplate;

    private final Parser parserCompiler;
    private final Styler stylerCompiler;
    private final StrategoRuntime strategoRuntimeCompiler;
    private final ConstraintAnalyzer constraintAnalyzerCompiler;

    public AdapterProject(
        TemplateCompiler templateCompiler,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("adapter_project/build.gradle.kts.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("adapter_project/package-info.java.mustache");
        this.checkTaskDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CheckTaskDef.java.mustache");
        this.componentTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Component.java.mustache");
        this.moduleTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Module.java.mustache");
        this.instanceTemplate = templateCompiler.getOrCompileToWriter("adapter_project/Instance.java.mustache");
        this.commandDefTemplate = templateCompiler.getOrCompileToWriter("adapter_project/CommandDef.java.mustache");

        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
    }

    public void generateBuildGradleKts(Input input) throws IOException {
        final Shared shared = input.shared();

        final HashMap<String, Object> map = new HashMap<>();

        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.api(input.languageProjectDependency()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCoreDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieDaggerDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.daggerDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
        map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));

        buildGradleTemplate.write(input, map, input.buildGradleKtsFile());
    }

    public void compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Files from other compilers.
        parserCompiler.compileAdapterProject(input.parser());
        try {
            input.styler().ifPresent((i) -> {
                try {
                    stylerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.strategoRuntime().ifPresent((i) -> {
                try {
                    strategoRuntimeCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.constraintAnalyzer().ifPresent((i) -> {
                try {
                    constraintAnalyzerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        // Collect all task definitions.
        final ArrayList<TypeInfo> allTaskDefs = new ArrayList<>(input.taskDefs());
        allTaskDefs.add(input.parser().tokenizeTaskDef());
        allTaskDefs.add(input.parser().parseTaskDef());
        if(input.styler().isPresent()) {
            allTaskDefs.add(input.styler().get().styleTaskDef());
        } else {
            allTaskDefs.add(TypeInfo.of("mb.spoofax.core.language.taskdef", "NullStyler"));
        }
        input.constraintAnalyzer().ifPresent((i) -> allTaskDefs.add(i.analyzeTaskDef()));
        allTaskDefs.add(input.checkTaskDef());

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(input, input.packageInfo().file(classesGenDirectory));
        checkTaskDefTemplate.write(input, input.checkTaskDef().file(classesGenDirectory));
        componentTemplate.write(input, input.genComponent().file(classesGenDirectory));
        for(CommandDefRepr commandDef : input.commandDefs()) {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("taskDefInjection", uniqueNamer.makeUnique(commandDef.taskDefType()));
            commandDefTemplate.write(commandDef, map, commandDef.type().file(classesGenDirectory));
        }

        {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("providedTaskDefs", allTaskDefs.stream().map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedCommandDefs", input.commandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            uniqueNamer.reset(); // New method scope
            map.put("providedAutoCommandDefs", input.autoCommandDefs().stream().map((c) -> uniqueNamer.makeUnique(c, c.commandDef().asVariableId())).collect(Collectors.toList()));
            moduleTemplate.write(input, map, input.genModule().file(classesGenDirectory));
        }
        {
            final UniqueNamer uniqueNamer = new UniqueNamer();
            uniqueNamer.reserve("fileExtensions");
            uniqueNamer.reserve("commandDefs");
            uniqueNamer.reserve("autoCommandDefs");
            final HashMap<String, Object> map = new HashMap<>();

            // Collect all injections.
            final ArrayList<NamedTypeInfo> injected = new ArrayList<>();
            map.put("injected", injected);

            // Create injections for tasks required in the language instance.
            final NamedTypeInfo parseInjection = uniqueNamer.makeUnique(input.parser().parseTaskDef());
            map.put("parseInjection", parseInjection);
            injected.add(parseInjection);
            final NamedTypeInfo tokenizeInjection = uniqueNamer.makeUnique(input.parser().tokenizeTaskDef());
            map.put("tokenizeInjection", tokenizeInjection);
            injected.add(tokenizeInjection);
            final NamedTypeInfo checkInjection = uniqueNamer.makeUnique(input.checkTaskDef());
            injected.add(checkInjection);
            map.put("checkInjection", checkInjection);
            final NamedTypeInfo styleInjection;
            if(input.styler().isPresent()) {
                styleInjection = uniqueNamer.makeUnique(input.styler().get().styleTaskDef());
            } else {
                styleInjection = uniqueNamer.makeUnique(TypeInfo.of("mb.spoofax.core.language.taskdef", "NullStyler"));
            }
            map.put("styleInjection", styleInjection);
            injected.add(styleInjection);

            // Create injections for all command definitions. TODO: only inject needed command definitions?
            injected.addAll(input.commandDefs().stream().map(CommandDefRepr::type).map(uniqueNamer::makeUnique).collect(Collectors.toList()));
            // Provide a lambda that gets the name of the injected command definition from the context.
            map.put("getInjectedCommandDef", (Mustache.Lambda)(frag, out) -> {
                final TypeInfo type = (TypeInfo)frag.context();
                final @Nullable String name = uniqueNamer.getNameFor(type);
                if(name == null) {
                    throw new IllegalStateException("Cannot get injected command definition for type '" + type + "'");
                }
                out.write(name);
            });

            instanceTemplate.write(input, map, input.genInstance().file(classesGenDirectory));
        }
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends AdapterProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        Parser.Input parser();

        Optional<Styler.Input> styler();

        Optional<StrategoRuntime.Input> strategoRuntime();

        Optional<ConstraintAnalyzer.Input> constraintAnalyzer();


        /// Configuration

        GradleDependency languageProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<TypeInfo> taskDefs();

        List<CommandDefRepr> commandDefs();

        List<AutoCommandDefRepr> autoCommandDefs();

        @Value.Default default CliCommandRepr cliCommand() {
            return CliCommandRepr.builder().name(shared().name()).build();
        }

        @Value.Default default List<MenuItemRepr> mainMenuItems() {
            return editorContextMenuItems();
        }

        List<MenuItemRepr> resourceContextMenuItems();

        List<MenuItemRepr> editorContextMenuItems();


        /// Gradle files

        default ResourcePath gradleGenDirectory() {
            return shared().adapterProject().baseDirectory();
        }

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return gradleGenDirectory().appendRelativePath("build.gradle.kts");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return shared().adapterProject().genSourceSpoofaxJavaDirectory();
        }


        /// Adapter project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(shared().adapterPackage(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }

        // Dagger component

        @Value.Default default TypeInfo genComponent() {
            return TypeInfo.of(shared().adapterPackage(), shared().classPrefix() + "Component");
        }

        Optional<TypeInfo> manualComponent();

        default TypeInfo component() {
            if(classKind().isManual() && manualComponent().isPresent()) {
                return manualComponent().get();
            }
            return genComponent();
        }

        default TypeInfo daggerComponent() {
            return TypeInfo.of(component().packageId(), "Dagger" + component().id());
        }

        // Dagger module

        @Value.Default default TypeInfo genModule() {
            return TypeInfo.of(shared().adapterPackage(), shared().classPrefix() + "Module");
        }

        Optional<TypeInfo> manualModule();

        default TypeInfo module() {
            if(classKind().isManual() && manualModule().isPresent()) {
                return manualModule().get();
            }
            return genModule();
        }

        // Language instance

        @Value.Default default TypeInfo genInstance() {
            return TypeInfo.of(shared().adapterPackage(), shared().classPrefix() + "Instance");
        }

        Optional<TypeInfo> manualInstance();

        default TypeInfo instance() {
            if(classKind().isManual() && manualInstance().isPresent()) {
                return manualInstance().get();
            }
            return genInstance();
        }


        /// Adapter project task definitions

        // Check task definition

        @Value.Default default TypeInfo genCheckTaskDef() {
            return TypeInfo.of(shared().adapterTaskPackage(), shared().classPrefix() + "Check");
        }

        Optional<TypeInfo> manualCheckTaskDef();

        default TypeInfo checkTaskDef() {
            if(classKind().isManual() && manualCheckTaskDef().isPresent()) {
                return manualCheckTaskDef().get();
            }
            return genCheckTaskDef();
        }


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualComponent().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualComponent' has not been set");
            }
            if(!manualModule().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualModule' has not been set");
            }
            if(!manualInstance().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualInstance' has not been set");
            }
            if(!manualCheckTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualCheckTaskDef' has not been set");
            }
        }
    }
}
