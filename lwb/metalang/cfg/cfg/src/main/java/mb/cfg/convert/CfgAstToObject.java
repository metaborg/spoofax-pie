package mb.cfg.convert;

import mb.cfg.CompileLanguageInput;
import mb.cfg.CompileLanguageInputCustomizer;
import mb.cfg.CompileLanguageSpecificationInput;
import mb.cfg.CompileLanguageSpecificationInputBuilder;
import mb.cfg.CompileLanguageSpecificationShared;
import mb.cfg.metalang.CompileEsvInput;
import mb.cfg.metalang.CompileSdf3Input;
import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.metalang.CompileStrategoInput;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.option.Option;
import mb.common.util.Properties;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.HoverAdapterCompiler;
import mb.spoofax.compiler.adapter.MultilangAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.adapter.ReferenceResolutionAdapterCompiler;
import mb.spoofax.compiler.adapter.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.CommandActionRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.CommandRequestRepr;
import mb.spoofax.compiler.adapter.data.MenuItemRepr;
import mb.spoofax.compiler.adapter.data.ParamRepr;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.ExportsLanguageCompiler;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.language.ParserVariant;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.EditorFileType;
import mb.spoofax.core.language.command.EditorSelectionType;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts a CFG AST into an {@link Output} containing messages, a {@link CompileLanguageInput} output object, and
 * properties that need to be written to a lockfile.
 */
public class CfgAstToObject {
    public static class Output {
        public final KeyedMessages messages;
        public final CompileLanguageInput compileLanguageInput;
        public final Properties properties;

        public Output(KeyedMessages messages, CompileLanguageInput compileLanguageInput, Properties properties) {
            this.messages = messages;
            this.compileLanguageInput = compileLanguageInput;
            this.properties = properties;
        }
    }

    public static Output convert(
        ResourcePath rootDirectory,
        @Nullable ResourceKey cfgFile,
        IStrategoTerm normalizedAst,
        Properties properties,
        CompileLanguageInputCustomizer customizer
    ) throws InvalidAstShapeException, IllegalStateException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final IStrategoList taskDefList = TermUtils.asListAt(normalizedAst, 0).orElseThrow(() -> new InvalidAstShapeException("task definition list as first subterm", normalizedAst));
        final IStrategoList commandDefList = TermUtils.asListAt(normalizedAst, 1).orElseThrow(() -> new InvalidAstShapeException("command definition list as second subterm", normalizedAst));
        final IStrategoList partsList = TermUtils.asListAt(normalizedAst, 2).orElseThrow(() -> new InvalidAstShapeException("parts list as third subterm", normalizedAst));
        final Parts parts = new Parts(messagesBuilder, cfgFile, partsList);

        // Shared
        final Shared.Builder sharedBuilder = Shared.builder().withPersistentProperties(properties);
        parts.forOneSubtermAsString("Group", sharedBuilder::defaultGroupId);
        parts.forOneSubtermAsString("Id", sharedBuilder::defaultArtifactId);
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
        customizer.customize(sharedBuilder);
        final Shared shared = sharedBuilder.build();

        // CompileLanguageInput builder
        final CompileLanguageInput.Builder compileLanguageInputBuilder = CompileLanguageInput.builder()
            .shared(shared);

        // LanguageBaseShared & LanguageAdapterShared
        final LanguageProject.Builder languageBaseSharedBuilder = LanguageProject.builder()
            .withDefaults(rootDirectory, shared);
        final AdapterProject.Builder languageAdapterSharedBuilder = AdapterProject.builder()
            .withDefaults(rootDirectory, shared);
        // TODO: properties
        customizer.customize(languageBaseSharedBuilder);
        final LanguageProject languageBaseShared = languageBaseSharedBuilder.build();
        customizer.customize(languageAdapterSharedBuilder);
        final AdapterProject languageAdapterShared = languageAdapterSharedBuilder.build();

        // LanguageShared
        final CompileLanguageSpecificationShared.Builder languageSharedBuilder = CompileLanguageSpecificationShared.builder()
            .languageProject(languageBaseShared);
        // TODO: includeLibSpoofax2Exports
        // TODO: includeLibStatixExports
        customizer.customize(languageSharedBuilder);
        final CompileLanguageSpecificationShared languageShared = languageSharedBuilder.build();

        // Builders for LanguageBaseCompilerInput & LanguageCompilerInput
        final LanguageProjectCompilerInputBuilder baseBuilder = new LanguageProjectCompilerInputBuilder();
        final AdapterProjectCompilerInputBuilder adapterBuilder = new AdapterProjectCompilerInputBuilder();

        // LanguageCompilerInput
        final CompileLanguageSpecificationInputBuilder languageCompilerInputBuilder = new CompileLanguageSpecificationInputBuilder();
        parts.getAllSubTermsInListAsParts("Sdf3Section").ifSome(subParts -> {
            final CompileSdf3Input.Builder builder = languageCompilerInputBuilder.withSdf3();
            subParts.forOneSubtermAsPath("Sdf3MainSourceDirectory", rootDirectory, builder::mainSourceDirectory);
            subParts.forOneSubtermAsPath("Sdf3MainFile", rootDirectory, builder::mainFile);
            subParts.getAllSubTermsInListAsParts("Sdf3ParseTableGeneratorSection").ifSome(ptgParts -> {
                ptgParts.forOneSubtermAsBool("Sdf3ParseTableGeneratorDynamic", builder::createDynamicParseTable);
                ptgParts.forOneSubtermAsBool("Sdf3ParseTableGeneratorDataDependent", builder::createDataDependentParseTable);
                ptgParts.forOneSubtermAsBool("Sdf3ParseTableGeneratorLayoutSensitive", builder::createLayoutSensitiveParseTable);
                ptgParts.forOneSubtermAsBool("Sdf3ParseTableGeneratorSolveDeepConflicts", builder::solveDeepConflictsInParseTable);
                ptgParts.forOneSubtermAsBool("Sdf3ParseTableGeneratorCheckOverlap", builder::checkOverlapInParseTable);
                ptgParts.forOneSubtermAsBool("Sdf3ParseTableGeneratorCheckPriorities", builder::checkPrioritiesInParseTable);
            });
        });
        parts.getAllSubTermsInListAsParts("EsvSection").ifSome(subParts -> {
            final CompileEsvInput.Builder builder = languageCompilerInputBuilder.withEsv();
            subParts.forOneSubtermAsPath("EsvMainSourceDirectory", rootDirectory, builder::mainSourceDirectory);
            subParts.forOneSubtermAsPath("EsvMainFile", rootDirectory, builder::mainFile);
            subParts.forAllSubtermsAsPaths("EsvIncludeDirectory", rootDirectory, builder::addIncludeDirectories);
        });
        parts.getAllSubTermsInListAsParts("StatixSection").ifSome(subParts -> {
            final CompileStatixInput.Builder builder = languageCompilerInputBuilder.withStatix();
            subParts.forOneSubtermAsPath("StatixMainSourceDirectory", rootDirectory, builder::mainSourceDirectory);
            subParts.forOneSubtermAsPath("StatixMainFile", rootDirectory, builder::mainFile);
            subParts.forOneSubtermAsBool("StatixSdf3SignatureGen", builder::enableSdf3SignatureGen);
        });
        parts.getAllSubTermsInListAsParts("StrategoSection").ifSome(subParts -> {
            final CompileStrategoInput.Builder builder = languageCompilerInputBuilder.withStratego();
            subParts.forOneSubtermAsPath("StrategoMainSourceDirectory", rootDirectory, builder::mainSourceDirectory);
            subParts.forOneSubtermAsPath("StrategoMainFile", rootDirectory, builder::mainFile);
            subParts.forOneSubtermAsString("StrategoLanguageStrategyAffix", builder::languageStrategyAffix);
            subParts.forOneSubtermAsBool("StrategoSdf3StatixExplicationGen", builder::enableSdf3StatixExplicationGen);
        });
        customizer.customize(languageCompilerInputBuilder);
        final CompileLanguageSpecificationInput languageCompilerInput = languageCompilerInputBuilder.build(properties, shared, languageShared);
        languageCompilerInput.syncTo(baseBuilder);
        compileLanguageInputBuilder.compileLanguageSpecificationInput(languageCompilerInput);

        // LanguageBaseCompilerInput & LanguageAdapterCompilerInput
        parts.getAllSubTermsInListAsParts("ParserSection").ifSome(subParts -> {
            final ParserLanguageCompiler.Input.Builder base = baseBuilder.withParser();
            subParts.forOneSubtermAsString("DefaultStartSymbol", base::startSymbol);
            subParts.getOneSubterm("ParserVariant").ifSome(variant -> {
                if(TermUtils.isAppl(variant, "Jsglr1", 0)) {
                    base.variant(ParserVariant.jsglr1());
                    return;
                }
                if(TermUtils.isAppl(variant, "Jsglr2", 1)) {
                    base.variant(ParserVariant.jsglr2());
                    final Parts variantParts = subParts.subParts(variant.getSubterm(0));
                    variantParts.getOneSubterm("Jsglr2Preset").ifSome(presetTerm -> {
                        final IStrategoAppl appl = TermUtils.asAppl(presetTerm)
                            .orElseThrow(() -> new InvalidAstShapeException("constructor application", presetTerm));
                        final ParserVariant.Jsglr2Preset preset;
                        switch(appl.getConstructor().getName()) {
                            default:
                            case "Jsglr2StandardPreset":
                                preset = ParserVariant.Jsglr2Preset.Standard;
                                break;
                            case "Jsglr2ElkhoundPreset":
                                preset = ParserVariant.Jsglr2Preset.Elkhound;
                                break;
                            case "Jsglr2RecoveryPreset":
                                preset = ParserVariant.Jsglr2Preset.Recovery;
                                break;
                            case "Jsglr2RecoveryElkhoundPreset":
                                preset = ParserVariant.Jsglr2Preset.RecoveryElkhound;
                                break;
                            case "Jsglr2DataDependentPreset":
                                preset = ParserVariant.Jsglr2Preset.DataDependent;
                                break;
                            case "Jsglr2LayoutSensitivePreset":
                                preset = ParserVariant.Jsglr2Preset.LayoutSensitive;
                                break;
                            case "Jsglr2CompositePreset":
                                preset = ParserVariant.Jsglr2Preset.Composite;
                                break;
                            case "Jsglr2IncrementalPreset":
                                preset = ParserVariant.Jsglr2Preset.Incremental;
                                break;
                            case "Jsglr2IncrementalRecoveryPreset":
                                preset = ParserVariant.Jsglr2Preset.IncrementalRecovery;
                                break;
                        }
                        base.variant(ParserVariant.jsglr2(preset));
                    });
                }
            });
            // TODO: parser language properties
            final ParserAdapterCompiler.Input.Builder adapter = adapterBuilder.withParser();
            // TODO: parser adapter properties
        });
        parts.getAllSubTermsInListAsParts("StylerSection").ifSome(subParts -> {
            final StylerLanguageCompiler.Input.Builder base = baseBuilder.withStyler();
            // TODO: styler language properties
            final StylerAdapterCompiler.Input.Builder adapter = adapterBuilder.withStyler();
            // TODO: styler adapter properties
        });
        parts.getAllSubTermsInListAsParts("ConstraintAnalyzerSection").ifSome(subParts -> {
            final ConstraintAnalyzerLanguageCompiler.Input.Builder base = baseBuilder.withConstraintAnalyzer();
            subParts.forOneSubtermAsBool("ConstraintAnalyzerEnableNaBL2", base::enableNaBL2);
            subParts.forOneSubtermAsBool("ConstraintAnalyzerEnableStatix", base::enableStatix);
            subParts.forOneSubtermAsBool("ConstraintAnalyzerMultiFile", base::multiFile);
            subParts.forOneSubtermAsString("ConstraintAnalyzerStrategoStrategy", base::strategoStrategy);
            // TODO: more constraintAnalyzer language properties
            final ConstraintAnalyzerAdapterCompiler.Input.Builder adapter = adapterBuilder.withConstraintAnalyzer();
            // TODO: constraintAnalyzer adapter properties
        });
        parts.getAllSubTermsInListAsParts("MultilangAnalyzerSection").ifSome(subParts -> {
            final MultilangAnalyzerLanguageCompiler.Input.Builder base = baseBuilder.withMultilangAnalyzer();
            // TODO: multilangAnalyzer language properties
            final MultilangAnalyzerAdapterCompiler.Input.Builder adapter = adapterBuilder.withMultilangAnalyzer();
            // TODO: multilangAnalyzer adapter properties
        });
        parts.getAllSubTermsInListAsParts("StrategoRuntimeSection").ifSome(subParts -> {
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
        parts.getAllSubTermsInListAsParts("ExportsSection").ifSome(subParts -> {
            final ExportsLanguageCompiler.Input.Builder builder = baseBuilder.withExports();
            // TODO: exports language properties
        });
        parts.getAllSubTermsInListAsParts("ReferenceResolutionSection").ifSome(subParts -> {
            final ReferenceResolutionAdapterCompiler.Input.Builder builder = adapterBuilder.withReferenceResolution();
            subParts.getOneSubterm("ReferenceResolutionVariant").ifSome(variant -> {
                TermUtils.asJavaStringAt(variant.getSubterm(0), 0).ifPresent(builder::resolveStrategy);
            });
        });
        parts.getAllSubTermsInListAsParts("HoverSection").ifSome(subParts -> {
            final HoverAdapterCompiler.Input.Builder builder = adapterBuilder.withHover();
            subParts.getOneSubterm("HoverVariant").ifSome(variant -> {
                TermUtils.asJavaStringAt(variant.getSubterm(0), 0).ifPresent(builder::hoverStrategy);
            });
        });

        // LanguageAdapterCompilerInput > Task definitions
        for(IStrategoTerm taskDefTerm : taskDefList) {
            final String type = TermUtils.asJavaStringAt(taskDefTerm, 0).orElseThrow(() -> new InvalidAstShapeException("Java type id as first subterm", taskDefTerm));
            final TypeInfo typeInfo = TypeInfo.of(type);
            adapterBuilder.project.addTaskDefs(typeInfo);
        }

        // LanguageAdapterCompilerInput > Command definitions
        for(IStrategoTerm commandDefTerm : commandDefList) {
            final Parts commandDefParts = parts.subParts(commandDefTerm.getSubterm(0));
            final CommandDefRepr.Builder commandDefBuilder = CommandDefRepr.builder();
            final TypeInfo taskDefType = getCommandDefTaskType(commandDefParts, commandDefTerm);
            commandDefBuilder.taskDefType(taskDefType);
            commandDefBuilder.type(getCommandDefType(commandDefParts, languageAdapterShared, taskDefType));
            commandDefBuilder.displayName(getCommandDefDisplayName(commandDefParts, commandDefTerm));
            getCommandDefDescription(commandDefParts).ifSome(commandDefBuilder::description);
            commandDefParts.forOneSubterm("CommandDefSupportedExecutionTypes", types -> types.forEach(term -> {
                commandDefBuilder.addSupportedExecutionTypes(toCommandExecutionType(term));
            }));
            commandDefParts.getAllSubTermsInListAsParts("CommandDefParameters").ifSome(parametersParts -> {
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
        }

        // TODO: adapt to new AST shape, set sane defaults, and ensure that menus are merged!
        // LanguageAdapterCompilerInput > Menus
        parts.forAllSubTermsInList("MainMenu", menuItem -> {
            adapterBuilder.project.addMainMenuItems(toMenuItemRepr(parts, menuItem, languageAdapterShared));
        });
        parts.forAllSubTermsInList("ResourceContextMenu", menuItem -> {
            adapterBuilder.project.addResourceContextMenuItems(toMenuItemRepr(parts, menuItem, languageAdapterShared));
        });
        parts.forAllSubTermsInList("EditorContextMenu", menuItem -> {
            adapterBuilder.project.addEditorContextMenuItems(toMenuItemRepr(parts, menuItem, languageAdapterShared));
        });
        customizer.customize(baseBuilder);
        final LanguageProjectCompiler.Input languageBaseCompilerInput = baseBuilder.build(shared, languageBaseShared);
        compileLanguageInputBuilder.languageProjectInput(languageBaseCompilerInput);
        customizer.customize(adapterBuilder);
        final AdapterProjectCompiler.Input languageAdapterCompilerInput = adapterBuilder.build(languageBaseCompilerInput, Option.ofNone(), languageAdapterShared);
        compileLanguageInputBuilder.adapterProjectInput(languageAdapterCompilerInput);

        // EclipseProjectCompiler.Input
        parts.getAllSubTermsInListAsParts("EclipseSection").ifElse(subParts -> {
            final EclipseProjectCompiler.Input.Builder builder = EclipseProjectCompiler.Input.builder()
                .withDefaultsSameProject(rootDirectory, shared)
                .languageProjectCompilerInput(languageBaseCompilerInput)
                .adapterProjectCompilerInput(languageAdapterCompilerInput);
            subParts.forOneSubtermAsTypeInfo("EclipseBaseLanguage", builder::baseLanguage);
            subParts.forOneSubtermAsTypeInfo("EclipseExtendLanguage", builder::extendLanguage);
            if(customizer.customize(builder)) {
                final EclipseProjectCompiler.Input input = builder.build();
                compileLanguageInputBuilder.eclipseProjectInput(input);
            }
        }, () -> {
            customizer.getDefaultEclipseProjectInput().ifPresent(builder -> {
                builder.withDefaultsSameProject(rootDirectory, shared)
                    .languageProjectCompilerInput(languageBaseCompilerInput)
                    .adapterProjectCompilerInput(languageAdapterCompilerInput);
                if(customizer.customize(builder)) {
                    final EclipseProjectCompiler.Input input = builder.build();
                    compileLanguageInputBuilder.eclipseProjectInput(input);
                }
            });
        });

        // Build compile language input object
        customizer.customize(compileLanguageInputBuilder);
        final CompileLanguageInput compileLanguageInput = compileLanguageInputBuilder.build();
        compileLanguageInput.savePersistentProperties(properties);

        // TODO: remove used parts and check to see that there are no leftover parts in the end? Or at least put warnings/errors on those?

        final Output output = new Output(messagesBuilder.build(), compileLanguageInput, properties);
        return output;
    }


    private static TypeInfo taskDefToTypeInfo(IStrategoTerm taskDefTerm) {
        if(!TermUtils.isAppl(taskDefTerm, "TaskDefExpr", 1) && !TermUtils.isAppl(taskDefTerm, "TaskDefPart", 1)) {
            throw new InvalidAstShapeException("a TaskDefExpr/TaskDefPart term application", taskDefTerm);
        }
        final IStrategoTerm taskDefSubTerm = taskDefTerm.getSubterm(0);
        if(!TermUtils.isAppl(taskDefSubTerm, "TaskDef", 1)) {
            throw new InvalidAstShapeException("a TaskDef term application", taskDefSubTerm);
        }
        return TypeInfo.of(TermUtils.toJavaStringAt(taskDefSubTerm, 0));
    }

    private static IStrategoTerm getActualCommandDefTerm(IStrategoTerm term) {
        if(TermUtils.isAppl(term, "CommandDefExpr", 1)) {
            return term.getSubterm(0);
        } else {
            return term;
        }
    }

    private static TypeInfo getCommandDefTaskType(Parts commandDefParts, IStrategoTerm commandDefTerm) {
        // CommandDefTaskDef must be set, ensured by static semantics.
        final IStrategoTerm taskDefTerm = commandDefParts.getOneSubterm("CommandDefTaskDef")
            .unwrapOrElseThrow(() -> new InvalidAstShapeException("command definition with one CommandDefTaskDef option", commandDefTerm));
        return taskDefToTypeInfo(taskDefTerm);
    }

    private static TypeInfo getCommandDefType(Parts commandDefParts, AdapterProject languageAdapterShared, TypeInfo taskDefType) {
        // Get type from command definition, or default to: <command package>.<task definition name>Command.
        return commandDefParts.getOneSubtermAsTypeInfo("CommandDefType").unwrapOrElse(() -> TypeInfo.of(languageAdapterShared.commandPackageId(), taskDefType.id() + "Command"));
    }

    private static String getCommandDefDisplayName(Parts commandDefParts, IStrategoTerm commandDefTerm) {
        // CommandDefDisplayName must be set, ensured by static semantics.
        return commandDefParts.getOneSubtermAsString("CommandDefDisplayName")
            .unwrapOrElseThrow(() -> new InvalidAstShapeException("command definition with one CommandDefDisplayName option", commandDefTerm));
    }

    private static Option<String> getCommandDefDescription(Parts commandDefParts) {
        return commandDefParts.getOneSubtermAsString("CommandDefDescription");
    }

    private static CommandExecutionType toCommandExecutionType(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("an ExecutionType term application", term));
        switch(appl.getConstructor().getName()) {
            case "Once":
                return CommandExecutionType.ManualOnce;
            case "Continuous":
                return CommandExecutionType.ManualContinuous;
            case "Automatic":
                return CommandExecutionType.AutomaticContinuous;
            default:
                throw new InvalidAstShapeException("a term of sort ExecutionType", appl);
        }
    }

    private static CommandContextType toCommandContextType(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("a term application", term));
        switch(appl.getConstructor().getName()) {
            case "ProjectContext":
                return CommandContextType.Project;
            case "DirectoryContext":
                return CommandContextType.Directory;
            case "FileContext":
                return CommandContextType.File;
            case "HierarchicalResourceContext":
                return CommandContextType.HierarchicalResource;
            case "ReadableResourceContext":
                return CommandContextType.ReadableResource;
            case "RegionContext":
                return CommandContextType.Region;
            case "OffsetContext":
                return CommandContextType.Offset;
            default:
                throw new InvalidAstShapeException("a term of sort CommandContext", appl);
        }
    }

    private static EnclosingCommandContextType toEnclosingCommandContextType(IStrategoTerm term) {
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

    private static ArgProviderRepr toParameterArgumentProvider(IStrategoTerm term) {
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

    private static MenuItemRepr toMenuItemRepr(Parts mainParts, IStrategoTerm menuItem, AdapterProject languageAdapterShared) {
        final IStrategoTerm actualMenuItem = menuItem.getSubterm(0); // Menu item expressions are always wrapped in a MenuItem(...) term.
        final IStrategoAppl appl = TermUtils.asAppl(actualMenuItem).orElseThrow(() -> new InvalidAstShapeException("a term application", actualMenuItem));
        switch(appl.getConstructor().getName()) {
            case "Separator":
                return MenuItemRepr.separator();
            case "Menu": {
                final String displayName = Parts.toJavaString(appl.getSubterm(0));
                final IStrategoList subMenuItemsTerm = TermUtils.asListAt(appl, 1)
                    .orElseThrow(() -> new InvalidAstShapeException("a list of sub-menu items as second subterm", appl));
                final List<MenuItemRepr> subMenuItems = subMenuItemsTerm.getSubterms().stream().map(t -> toMenuItemRepr(mainParts, t, languageAdapterShared)).collect(Collectors.toList());
                return MenuItemRepr.menu(displayName, subMenuItems);
            }
            case "CommandAction": {
                final IStrategoList properties = TermUtils.asListAt(appl, 0)
                    .orElseThrow(() -> new InvalidAstShapeException("a list of command action properties as first subterm", appl));
                final Parts commandActionParts = mainParts.subParts(properties);
                final CommandActionRepr.Builder commandActionBuilder = CommandActionRepr.builder();

                // CommandActionDef must be set, ensured by static semantics.
                final IStrategoTerm commandDefTerm = getActualCommandDefTerm(commandActionParts.getOneSubterm("CommandActionDef")
                    .unwrapOrElseThrow(() -> new InvalidAstShapeException("command action with one CommandActionDef option", actualMenuItem)));
                final Parts commandDefParts = mainParts.subParts(commandDefTerm.getSubterm(0));
                final TypeInfo commandDefTaskType = getCommandDefTaskType(commandDefParts, commandDefTerm);
                final TypeInfo commandDefType = getCommandDefType(commandDefParts, languageAdapterShared, commandDefTaskType);

                final CommandRequestRepr.Builder commandRequestBuilder = CommandRequestRepr.builder();
                commandRequestBuilder.commandDefType(commandDefType);
                // CommandActionExecutionType must be set, ensured by static semantics.
                final CommandExecutionType commandExecutionType = commandActionParts.getOneSubterm("CommandActionExecutionType")
                    .map(CfgAstToObject::toCommandExecutionType)
                    .unwrapOrElseThrow(() -> new InvalidAstShapeException("command action with one CommandActionExecutionType option", actualMenuItem));
                commandRequestBuilder.executionType(commandExecutionType);
                // TODO: initial arguments
                commandActionBuilder.commandRequest(commandRequestBuilder.build());

                final String displayName = commandActionParts.getOneSubtermAsString("CommandActionDisplayName")
                    .unwrapOrElse(() -> getCommandDefDisplayName(commandDefParts, commandDefTerm) + (commandExecutionType == CommandExecutionType.ManualContinuous ? " (continuous)" : ""));
                commandActionBuilder.displayName(displayName);
                commandActionParts.getOneSubtermAsString("CommandActionDescription").orElse(() -> getCommandDefDescription(commandDefParts)).ifSome(commandActionBuilder::description);
                commandActionParts.forAllSubTermsInList("CommandActionRequiredEditorSelectionTypes", term -> commandActionBuilder.addRequiredEditorSelectionTypes(toEditorSelectionType(term)));
                commandActionParts.forAllSubTermsInList("CommandActionRequiredEditorFileTypes", term -> commandActionBuilder.addRequiredEditorFileTypes(toEditorFileType(term)));
                commandActionParts.forAllSubTermsInList("CommandActionRequiredHierarchicalResourceTypes", term -> commandActionBuilder.addRequiredResourceTypes(toHierarchicalResourceType(term)));
                commandActionParts.forAllSubTermsInList("CommandActionRequiredEnclosingResourceTypes", term -> commandActionBuilder.addRequiredEnclosingResourceTypes(toEnclosingCommandContextType(term)));
                return MenuItemRepr.commandAction(commandActionBuilder.build());
            }
            default:
                throw new InvalidAstShapeException("a term of sort MenuItem", appl);
        }
    }

    private static EditorSelectionType toEditorSelectionType(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("an EditorSelectionType term application", term));
        switch(appl.getConstructor().getName()) {
            case "Region":
                return EditorSelectionType.Region;
            case "Offset":
                return EditorSelectionType.Offset;
            default:
                throw new InvalidAstShapeException("a term of sort EditorSelectionType", appl);
        }
    }

    private static EditorFileType toEditorFileType(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("an EditorFileType term application", term));
        switch(appl.getConstructor().getName()) {
            case "HierarchicalResource":
                return EditorFileType.HierarchicalResource;
            case "ReadableResource":
                return EditorFileType.ReadableResource;
            default:
                throw new InvalidAstShapeException("a term of sort EditorFileType", appl);
        }
    }

    private static HierarchicalResourceType toHierarchicalResourceType(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term).orElseThrow(() -> new InvalidAstShapeException("an HierarchicalResourceType term application", term));
        switch(appl.getConstructor().getName()) {
            case "Project":
                return HierarchicalResourceType.Project;
            case "Directory":
                return HierarchicalResourceType.Directory;
            case "File":
                return HierarchicalResourceType.File;
            default:
                throw new InvalidAstShapeException("a term of sort HierarchicalResourceType", appl);
        }
    }
}

