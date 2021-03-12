package mb.cfg.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.MultiMap;
import mb.common.util.Properties;
import mb.common.util.StreamIterable;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.MultilangAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.adapter.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.ParamRepr;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.ExportsLanguageCompiler;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageInput;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageInputBuilder;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageShared;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageToJavaClassPathInput;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileEsvInput;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileSdf3Input;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileStatixInput;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileStrategoInput;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AstToObject {
    public static class Output {
        public final KeyedMessages messages;
        public final CompileLanguageToJavaClassPathInput compileLanguageToJavaClassPathInput;
        public final Properties properties;

        public Output(KeyedMessages messages, CompileLanguageToJavaClassPathInput compileLanguageToJavaClassPathInput, Properties properties) {
            this.messages = messages;
            this.compileLanguageToJavaClassPathInput = compileLanguageToJavaClassPathInput;
            this.properties = properties;
        }
    }

    public static Output convert(
        ResourcePath rootDirectory,
        @Nullable ResourceKey cfgFile,
        IStrategoTerm ast,
        Properties properties
    ) throws InvalidAstShapeException, IllegalStateException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final IStrategoList partsList = TermUtils.asListAt(ast, 0).orElseThrow(() -> new InvalidAstShapeException("part list as first subterm", ast));
        final Parts parts = new Parts(messagesBuilder, cfgFile, partsList);

        // Shared
        final Shared.Builder sharedBuilder = Shared.builder().withPersistentProperties(properties);
        parts.forOneSubtermAsString("Group", sharedBuilder::defaultGroupId);
        parts.forOneSubtermAsString("Name", sharedBuilder::name);
        parts.forOneSubtermAsString("Version", sharedBuilder::defaultVersion);
        parts.forAllSubtermsAsStrings("FileExtension", sharedBuilder::addFileExtensions);
        parts.forOneSubtermAsString("JavaPackageIdPrefix", prefix -> {
            if(prefix.endsWith(".")) {
                sharedBuilder.defaultPackageIdPrefix(prefix);
            } else {
                sharedBuilder.defaultPackageIdPrefix(prefix + ".");
            }
        });
        parts.forOneSubtermAsString("JavaClassIdPrefix", sharedBuilder::defaultClassPrefix);
        // TODO: source directory
        // TODO: build directory
        final Shared shared = sharedBuilder.build();

        // LanguageBaseShared & LanguageAdapterShared
        final LanguageProject.Builder languageBaseSharedBuilder = LanguageProject.builder()
            .withDefaults(rootDirectory, shared);
        final AdapterProject.Builder languageAdapterSharedBuilder = AdapterProject.builder()
            .withDefaults(rootDirectory, shared);
        // TODO: properties
        final LanguageProject languageBaseShared = languageBaseSharedBuilder.build();
        final AdapterProject languageAdapterShared = languageAdapterSharedBuilder.build();

        // LanguageShared
        final CompileLanguageShared.Builder languageSharedBuilder = CompileLanguageShared.builder()
            .languageProject(languageBaseShared);
        // TODO: includeLibSpoofax2Exports
        // TODO: includeLibStatixExports
        final CompileLanguageShared languageShared = languageSharedBuilder.build();

        // Builders for LanguageBaseCompilerInput & LanguageCompilerInput
        final LanguageProjectCompilerInputBuilder baseBuilder = new LanguageProjectCompilerInputBuilder();
        final AdapterProjectCompilerInputBuilder adapterBuilder = new AdapterProjectCompilerInputBuilder();

        // LanguageCompilerInput
        final CompileLanguageInputBuilder languageCompilerInputBuilder = new CompileLanguageInputBuilder();
        parts.getAllSubTermsInListAsParts("Sdf3Section").ifPresent(subParts -> {
            final CompileSdf3Input.Builder builder = languageCompilerInputBuilder.withSdf3();
            subParts.forOneSubtermAsString("Sdf3StrategoStrategyAffix", builder::strategoStrategyIdAffix);
            // TODO: more SDF3 properties
        });
        parts.getAllSubTermsInListAsParts("EsvSection").ifPresent(subParts -> {
            final CompileEsvInput.Builder builder = languageCompilerInputBuilder.withEsv();
            // TODO: ESV properties
        });
        parts.getAllSubTermsInListAsParts("StatixSection").ifPresent(subParts -> {
            final CompileStatixInput.Builder builder = languageCompilerInputBuilder.withStatix();
            // TODO: Statix properties
        });
        parts.getAllSubTermsInListAsParts("StrategoSection").ifPresent(subParts -> {
            final CompileStrategoInput.Builder builder = languageCompilerInputBuilder.withStratego();
            // TODO: Stratego properties
        });
        final CompileLanguageInput languageCompilerInput = languageCompilerInputBuilder.build(properties, shared, languageShared);
        languageCompilerInput.syncTo(baseBuilder);

        // LanguageBaseCompilerInput & LanguageAdapterCompilerInput
        parts.getAllSubTermsInListAsParts("ParserSection").ifPresent(subParts -> {
            final ParserLanguageCompiler.Input.Builder base = baseBuilder.withParser();
            subParts.forOneSubtermAsString("DefaultStartSymbol", base::startSymbol);
            // TODO: parser language properties
            final ParserAdapterCompiler.Input.Builder adapter = adapterBuilder.withParser();
            // TODO: parser adapter properties
        });
        parts.getAllSubTermsInListAsParts("StylerSection").ifPresent(subParts -> {
            final StylerLanguageCompiler.Input.Builder base = baseBuilder.withStyler();
            // TODO: styler language properties
            final StylerAdapterCompiler.Input.Builder adapter = adapterBuilder.withStyler();
            // TODO: styler adapter properties
        });
        parts.getAllSubTermsInListAsParts("ConstraintAnalyzerSection").ifPresent(subParts -> {
            final ConstraintAnalyzerLanguageCompiler.Input.Builder base = baseBuilder.withConstraintAnalyzer();
            subParts.forOneSubtermAsBool("ConstraintAnalyzerEnableNaBL2", base::enableNaBL2);
            subParts.forOneSubtermAsBool("ConstraintAnalyzerEnableStatix", base::enableStatix);
            subParts.forOneSubtermAsBool("ConstraintAnalyzerMultiFile", base::multiFile);
            subParts.forOneSubtermAsString("ConstraintAnalyzerStrategoStrategy", base::strategoStrategy);
            // TODO: more constraintAnalyzer language properties
            final ConstraintAnalyzerAdapterCompiler.Input.Builder adapter = adapterBuilder.withConstraintAnalyzer();
            // TODO: constraintAnalyzer adapter properties
        });
        parts.getAllSubTermsInListAsParts("MultilangAnalyzerSection").ifPresent(subParts -> {
            final MultilangAnalyzerLanguageCompiler.Input.Builder base = baseBuilder.withMultilangAnalyzer();
            // TODO: multilangAnalyzer language properties
            final MultilangAnalyzerAdapterCompiler.Input.Builder adapter = adapterBuilder.withMultilangAnalyzer();
            // TODO: multilangAnalyzer adapter properties
        });
        parts.getAllSubTermsInListAsParts("StrategoRuntimeSection").ifPresent(subParts -> {
            final StrategoRuntimeLanguageCompiler.Input.Builder base = baseBuilder.withStrategoRuntime();
            subParts.forAllSubtermsAsStrings("StrategoRuntimeStrategyPackageId", base::addStrategyPackageIds);
            subParts.forAllSubtermsAsStrings("StrategoRuntimeInteropRegistererByReflection", base::addInteropRegisterersByReflection);
            subParts.forOneSubtermAsBool("StrategoRuntimeAddSpoofax2Primitives", base::addSpoofax2Primitives);
            subParts.forOneSubtermAsBool("StrategoRuntimeAddNaBL2Primitives", base::addNaBL2Primitives);
            subParts.forOneSubtermAsBool("StrategoRuntimeAddStatixPrimitives", base::addStatixPrimitives);
            // TODO: more strategoRuntime language properties
            final StrategoRuntimeAdapterCompiler.Input.Builder adapter = adapterBuilder.withStrategoRuntime();
            // TODO: strategoRuntime adapter properties
        });
        // TODO: completion
        parts.getAllSubTermsInListAsParts("ExportsSection").ifPresent(subParts -> {
            final ExportsLanguageCompiler.Input.Builder builder = baseBuilder.withExports();
            // TODO: exports language properties
        });
        parts.getAllSubTermsInListAsParts("TaskDefs").ifPresent(subParts ->
            subParts.forAllSubtermsAsTypeInfo("TaskDef", adapterBuilder.project::addTaskDefs)
        );
        parts.getAllSubTermsInListAsParts("CommandDef").ifPresent(commandDefParts -> {
            final CommandDefRepr.Builder commandDefBuilder = CommandDefRepr.builder();
            commandDefParts.forOneSubtermAsTypeInfo("CommandDefType", commandDefBuilder::type);
            commandDefParts.forOneSubtermAsTypeInfo("CommandDefTaskDefType", commandDefBuilder::taskDefType);
            commandDefParts.forOneSubtermAsTypeInfo("CommandDefArgsType", commandDefBuilder::argType);
            commandDefParts.forOneSubtermAsString("CommandDefDisplayName", commandDefBuilder::displayName);
            commandDefParts.forOneSubtermAsString("CommandDefDescription", commandDefBuilder::description);
            commandDefParts.forOneSubterm("CommandDefSupportedExecutionTypes", types -> types.forEach(term -> {
                commandDefBuilder.addSupportedExecutionTypes(toCommandExecutionType(term));
            }));
            commandDefParts.getAllSubTermsInListAsParts("CommandDefParameters").ifPresent(parametersParts -> {
                parametersParts.forAll("Parameter", 2, parameterTerm -> {
                    final ParamRepr.Builder parameterBuilder = ParamRepr.builder();
                    final String id = TermUtils.asJavaStringAt(parameterTerm, 0).orElseThrow(() -> new InvalidAstShapeException("id as first subterm", parameterTerm));
                    parameterBuilder.id(id);
                    final IStrategoList parameterProperties = TermUtils.asListAt(parameterTerm, 1).orElseThrow(() -> new InvalidAstShapeException("list as second subterm", parameterTerm));
                    final Parts parameterParts = new Parts(messagesBuilder, cfgFile, parameterProperties);
                    parameterParts.forOneSubtermAsTypeInfo("ParameterType", parameterBuilder::type);
                    parameterParts.forOneSubtermAsBool("ParameterRequired", parameterBuilder::required);
                    parameterParts.getAllSubTermsInList("ParameterArgumentProviders").forEach(parameterArgumentProviderTerm -> {
                        // NOTE: not using getAllSubTermsInListAsParts because order matters here.
                        parameterBuilder.addProviders(toParameterArgumentProvider(parameterArgumentProviderTerm));
                    });
                    commandDefBuilder.addParams(parameterBuilder.build());
                });
            });
            adapterBuilder.project.addCommandDefs(commandDefBuilder.build());
        });
        final LanguageProjectCompiler.Input languageBaseCompilerInput = baseBuilder.build(shared, languageBaseShared);
        final AdapterProjectCompiler.Input languageAdapterCompilerInput = adapterBuilder.build(languageBaseCompilerInput, Option.ofNone(), languageAdapterShared);

        // Final input object.
        final CompileLanguageToJavaClassPathInput.Builder compileLanguageToJavaClassPathInputBuilder = CompileLanguageToJavaClassPathInput.builder()
            .shared(shared)
            .languageProjectInput(languageBaseCompilerInput)
            .compileLanguageInput(languageCompilerInput)
            .adapterProjectInput(languageAdapterCompilerInput);
        // TODO: properties
        final CompileLanguageToJavaClassPathInput compileLanguageToJavaClassPathInput = compileLanguageToJavaClassPathInputBuilder.build();
        compileLanguageToJavaClassPathInput.savePersistentProperties(properties);

        // TODO: remove used parts and check to see that there are no leftover parts in the end? Or at least put warnings/errors on those?

        final Output output = new Output(messagesBuilder.build(), compileLanguageToJavaClassPathInput, properties);
        return output;
    }

    public static CommandExecutionType toCommandExecutionType(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("an ExecutionType term application", term));
        switch(appl.getConstructor().getName()) {
            case "ManualOnce":
                return CommandExecutionType.ManualOnce;
            case "ManualContinuous":
                return CommandExecutionType.ManualContinuous;
            case "AutomaticContinuous":
                return CommandExecutionType.AutomaticContinuous;
            default:
                throw new InvalidAstShapeException("a term of sort ExecutionType", appl);
        }
    }

    public static CommandContextType toCommandContextType(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("a term application", term));
        switch(appl.getConstructor().getName()) {
            case "ProjectContext":
                return CommandContextType.Project;
            case "DirectoryContext":
                return CommandContextType.Directory;
            case "FileContext":
                return CommandContextType.File;
            case "ResourcePathContext":
                return CommandContextType.ResourcePath;
            case "ResourceKeyContext":
                return CommandContextType.ResourceKey;
            case "RegionContext":
                return CommandContextType.Region;
            case "OffsetContext":
                return CommandContextType.Offset;
            default:
                throw new InvalidAstShapeException("a term of sort CommandContext", appl);
        }
    }

    public static EnclosingCommandContextType toEnclosingCommandContextType(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("a term application", term));
        switch(appl.getConstructor().getName()) {
            case "ProjectEnclosingContext":
                return EnclosingCommandContextType.Project;
            case "DirectoryEnclosingContext":
                return EnclosingCommandContextType.Directory;
            default:
                throw new InvalidAstShapeException("a term of sort EnclosingCommandContext", appl);
        }
    }

    public static ArgProviderRepr toParameterArgumentProvider(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("a term application", term));
        switch(appl.getConstructor().getName()) {
            case "ValueArgumentProvider":
                return ArgProviderRepr.value(TermUtils.asJavaStringAt(appl, 0).orElseThrow(() -> new InvalidAstShapeException("a string as first subterm", appl)));
            case "ContextArgumentProvider":
                return ArgProviderRepr.context(toCommandContextType(appl.getSubterm(0)));
            case "EnclosingContextArgumentProvider":
                return ArgProviderRepr.enclosingContext(toEnclosingCommandContextType(appl.getSubterm(0)));
            default:
                throw new InvalidAstShapeException("a term of sort ArgumentProvider", appl);
        }
    }
}

class Parts {
    private final KeyedMessagesBuilder messagesBuilder;
    private final @Nullable ResourceKey cfgFile;
    private final MultiMap<IStrategoConstructor, IStrategoAppl> parts;
    private final TermFactory termFactory = new TermFactory();

    Parts(
        KeyedMessagesBuilder messagesBuilder,
        @Nullable ResourceKey cfgFile,
        Iterable<IStrategoTerm> terms
    ) throws InvalidAstShapeException {
        this.messagesBuilder = messagesBuilder;
        this.cfgFile = cfgFile;
        final MultiMap<IStrategoConstructor, IStrategoAppl> parts = MultiMap.withLinkedHash();
        for(IStrategoTerm part : terms) {
            final IStrategoAppl partAppl = TermUtils.asAppl(part).orElseThrow(() -> new InvalidAstShapeException("part to be a term application", part));
            parts.put(partAppl.getConstructor(), partAppl);
        }
        this.parts = parts;
    }

    Parts(
        KeyedMessagesBuilder messagesBuilder,
        @Nullable ResourceKey cfgFile,
        Stream<IStrategoTerm> terms
    ) throws InvalidAstShapeException {
        this(messagesBuilder, cfgFile, new StreamIterable<>(terms));
    }


    boolean contains(IStrategoConstructor constructor) {
        return parts.containsKey(constructor);
    }

    boolean contains(String name, int arity) {
        return contains(termFactory.makeConstructor(name, arity));
    }

    boolean contains(String name) {
        return contains(name, 1);
    }


    Optional<IStrategoAppl> getOne(IStrategoConstructor constructor) {
        final ArrayList<IStrategoAppl> values = parts.get(constructor);
        if(values.isEmpty()) {
            return Optional.empty();
        } else if(values.size() == 1) {
            return Optional.of(values.get(0));
        } else {
            @MonotonicNonNull IStrategoAppl first = null;
            for(IStrategoAppl part : values) {
                if(first == null) {
                    first = part;
                } else {
                    createCfgWarning("Option ignored because it was defined before", part);
                }
            }
            return Optional.of(first);
        }
    }

    void forOne(IStrategoConstructor constructor, Consumer<IStrategoAppl> consumer) {
        getOne(constructor).ifPresent(consumer);
    }

    Optional<IStrategoAppl> getOne(String name, int arity) {
        return getOne(termFactory.makeConstructor(name, arity));
    }

    void forOne(String name, int arity, Consumer<IStrategoAppl> consumer) {
        getOne(name, arity).ifPresent(consumer);
    }


    Optional<IStrategoTerm> getOneSubterm(String name) {
        return getOne(name, 1).map(t -> t.getSubterm(0));
    }

    void forOneSubterm(String name, Consumer<IStrategoTerm> consumer) {
        getOneSubterm(name).ifPresent(consumer);
    }

    Optional<String> getOneSubtermAsString(String name) {
        return getOneSubterm(name).map(Parts::toJavaString);
    }

    void forOneSubtermAsString(String name, Consumer<String> consumer) {
        getOneSubtermAsString(name).ifPresent(consumer);
    }

    Optional<TypeInfo> getOneSubtermAsTypeInfo(String name) {
        return getOneSubtermAsString(name).map(TypeInfo::of);
    }

    void forOneSubtermAsTypeInfo(String name, Consumer<TypeInfo> consumer) {
        getOneSubtermAsTypeInfo(name).ifPresent(consumer);
    }

    Optional<Boolean> getOneSubtermAsBool(String name) {
        return getOneSubterm(name).flatMap(TermUtils::asAppl).map(a -> a.getConstructor().getName().equals("True"));
    }

    void forOneSubtermAsBool(String name, Consumer<Boolean> consumer) {
        getOneSubtermAsBool(name).ifPresent(consumer);
    }


    ArrayList<IStrategoAppl> getAll(IStrategoConstructor constructor) {
        return parts.get(constructor);
    }

    void forAll(IStrategoConstructor constructor, Consumer<IStrategoAppl> consumer) {
        getAll(constructor).forEach(consumer);
    }

    ArrayList<IStrategoAppl> getAll(String name, int arity) {
        return getAll(termFactory.makeConstructor(name, arity));
    }

    void forAll(String name, int arity, Consumer<IStrategoAppl> consumer) {
        getAll(name, arity).forEach(consumer);
    }


    Stream<IStrategoTerm> getAllSubTerms(String name) {
        return getAll(name, 1).stream().map(t -> t.getSubterm(0));
    }

    void forAllSubTerms(String name, Consumer<IStrategoTerm> consumer) {
        getAllSubTerms(name).forEach(consumer);
    }

    Stream<String> getAllSubTermsAsStrings(String name) {
        return getAllSubTerms(name).map(Parts::toJavaString);
    }

    void forAllSubtermsAsStrings(String name, Consumer<String> consumer) {
        getAllSubTermsAsStrings(name).forEach(consumer);
    }

    Stream<TypeInfo> getAllSubtermsAsTypeInfo(String name) {
        return getAllSubTermsAsStrings(name).map(TypeInfo::of);
    }

    void forAllSubtermsAsTypeInfo(String name, Consumer<TypeInfo> consumer) {
        getAllSubtermsAsTypeInfo(name).forEach(consumer);
    }

    Optional<Parts> getAllSubTermsAsParts(String name) {
        if(!contains(name)) return Optional.empty();
        return Optional.of(new Parts(messagesBuilder, cfgFile, getAllSubTerms(name)));
    }


    Stream<IStrategoTerm> getAllSubTermsInList(String name) {
        return getAll(name, 1).stream().flatMap(t -> t.getSubterm(0).getSubterms().stream());
    }

    void forAllSubTermsInList(String name, Consumer<IStrategoTerm> consumer) {
        getAllSubTermsInList(name).forEach(consumer);
    }

    Optional<Parts> getAllSubTermsInListAsParts(String name) {
        if(!contains(name)) return Optional.empty();
        return Optional.of(new Parts(messagesBuilder, cfgFile, getAllSubTermsInList(name)));
    }


    private @Nullable ResourceKey getCfgFile(IStrategoTerm term) {
        if(cfgFile != null) return cfgFile;
        return TermTracer.getResourceKey(term);
    }

    private void createCfgMessage(String text, Severity severity, IStrategoTerm term) {
        final @Nullable ResourceKey resource = getCfgFile(term);
        if(resource != null) {
            final @Nullable Region region = getRegion(term);
            if(region != null) {
                messagesBuilder.addMessage(text, severity, resource, region);
            } else {
                messagesBuilder.addMessage(text, severity, resource);
            }
        } else {
            messagesBuilder.addMessage(text, severity);
        }
    }

    private void createCfgError(String text, IStrategoTerm term) {
        createCfgMessage(text, Severity.Error, term);
    }

    private void createCfgWarning(String text, IStrategoTerm term) {
        createCfgMessage(text, Severity.Warning, term);
    }


    private static @Nullable Region getRegion(IStrategoTerm term) {
        return TermTracer.getRegion(term);
    }


    private static String toJavaString(IStrategoTerm term) {
        return removeDoubleQuotes(TermUtils.toJavaString(term));
    }

    private static String removeDoubleQuotes(String string) {
        if(string.startsWith("\"")) {
            string = string.substring(1);
        }
        if(string.endsWith("\"")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }
}
