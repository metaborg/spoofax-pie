package mb.cfg.convert;

import mb.aterm.common.InvalidAstShapeException;
import mb.cfg.CompileLanguageInput;
import mb.cfg.CompileLanguageInputCustomizer;
import mb.cfg.CompileLanguageSpecificationInput;
import mb.cfg.CompileLanguageSpecificationInputBuilder;
import mb.cfg.CompileLanguageSpecificationShared;
import mb.cfg.Dependency;
import mb.cfg.DependencyKind;
import mb.cfg.DependencySource;
import mb.cfg.metalang.CfgDynamixConfig;
import mb.cfg.metalang.CfgDynamixSource;
import mb.cfg.metalang.CfgEsvConfig;
import mb.cfg.metalang.CfgEsvSource;
import mb.cfg.metalang.CfgSdf3Config;
import mb.cfg.metalang.CfgSdf3Source;
import mb.cfg.metalang.CfgStatixConfig;
import mb.cfg.metalang.CfgStatixSource;
import mb.cfg.metalang.CfgStrategoConfig;
import mb.cfg.metalang.CfgStrategoSource;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.util.Properties;
import mb.common.util.SetView;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.common.BlockCommentSymbols;
import mb.spoofax.common.BracketSymbols;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.adapter.CodeCompletionAdapterCompiler;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.HoverAdapterCompiler;
import mb.spoofax.compiler.adapter.MultilangAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.adapter.ReferenceResolutionAdapterCompiler;
import mb.spoofax.compiler.adapter.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.adapter.TegoRuntimeAdapterCompiler;
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
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.Version;
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

import java.util.LinkedHashSet;
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
        ExecContext context,
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
        final Parts parts = new Parts(context, messagesBuilder, cfgFile, partsList);

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
            final CfgSdf3Config.Builder builder = languageCompilerInputBuilder.withSdf3();
            subParts.getOneSubterm("Sdf3Source").ifSome(source -> {
                if(TermUtils.isAppl(source, "Sdf3Files", 1)) {
                    final Parts filesParts = subParts.subParts(source.getSubterm(0));
                    final CfgSdf3Source.Files.Builder filesSourceBuilder = CfgSdf3Source.Files.builder().compileLanguageShared(languageShared);
                    final ResourcePath mainSourceDirectory = filesParts.getOneSubtermAsExistingDirectory("Sdf3FilesMainSourceDirectory", rootDirectory, "SDF3 main source directory")
                        .unwrapOrElse(() -> CfgSdf3Source.Files.Builder.getDefaultMainSourceDirectory(languageShared));
                    filesSourceBuilder.mainSourceDirectory(mainSourceDirectory);
                    filesParts.forOneSubtermAsExistingFile("Sdf3FilesMainFile", mainSourceDirectory, "SDF3 main file", filesSourceBuilder::mainFile);
                    builder.source(CfgSdf3Source.files(filesSourceBuilder.build()));
                } else if(TermUtils.isAppl(source, "Sdf3Prebuilt", 1)) {
                    final Parts prebuiltParts = subParts.subParts(source.getSubterm(0));
                    final Option<ResourcePath> atermFile = prebuiltParts.getOneSubtermAsExistingFile("Sdf3PrebuiltParseTableAtermFile", rootDirectory, "SDF3 prebuilt parse table ATerm file");
                    final Option<ResourcePath> persistedFile = prebuiltParts.getOneSubtermAsExistingFile("Sdf3PrebuiltParseTablePersistedFile", rootDirectory, "SDF3 prebuilt parse table persisted file");
                    if(!atermFile.isSome()) {
                        messagesBuilder.addMessage("parse-table-aterm-file = $Path option is missing", Severity.Error, cfgFile, TermTracer.getRegion(source));
                    }
                    if(!persistedFile.isSome()) {
                        messagesBuilder.addMessage("parse-table-persisted-file = $Path option is missing", Severity.Error, cfgFile, TermTracer.getRegion(source));
                    }
                    if(atermFile.isSome() && persistedFile.isSome()) {
                        builder.source(CfgSdf3Source.prebuilt(atermFile.unwrap(), persistedFile.unwrap()));
                    }
                } else {
                    throw new InvalidAstShapeException("SDF3 source", source);
                }
            });

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
            final CfgEsvConfig.Builder builder = languageCompilerInputBuilder.withEsv();
            subParts.getOneSubterm("EsvSource").ifSome(source -> {
                if(TermUtils.isAppl(source, "EsvFiles", 1)) {
                    final Parts filesParts = subParts.subParts(source.getSubterm(0));
                    final CfgEsvSource.Files.Builder filesSourceBuilder = CfgEsvSource.Files.builder().compileLanguageShared(languageShared);
                    final ResourcePath mainSourceDirectory = filesParts.getOneSubtermAsExistingDirectory("EsvFilesMainSourceDirectory", rootDirectory, "ESV main source directory")
                        .unwrapOrElse(() -> CfgEsvSource.Files.Builder.getDefaultMainSourceDirectory(languageShared));
                    filesSourceBuilder.mainSourceDirectory(mainSourceDirectory);
                    filesParts.forOneSubtermAsExistingFile("EsvFilesMainFile", mainSourceDirectory, "ESV main file", filesSourceBuilder::mainFile);
                    filesParts.forAllSubtermsAsExistingDirectories("EsvFilesIncludeDirectory", rootDirectory, "ESV include directory", filesSourceBuilder::addIncludeDirectories);
                    filesParts.forOneSubtermAsBool("EsvFilesIncludeLibspoofax2Exports", filesSourceBuilder::includeLibSpoofax2Exports);
                    builder.source(CfgEsvSource.files(filesSourceBuilder.build()));
                } else if(TermUtils.isAppl(source, "EsvPrebuilt", 1)) {
                    final Parts prebuiltParts = subParts.subParts(source.getSubterm(0));
                    prebuiltParts.getOneSubtermAsExistingFile("EsvPrebuiltFile", rootDirectory, "ESV prebuilt file").ifElse(
                        file -> builder.source(CfgEsvSource.prebuilt(file)),
                        () -> messagesBuilder.addMessage("file = $Path option is missing", Severity.Error, cfgFile, TermTracer.getRegion(source))
                    );
                } else {
                    throw new InvalidAstShapeException("ESV source", source);
                }
            });
        });
        parts.getAllSubTermsInListAsParts("StatixSection").ifSome(subParts -> {
            final CfgStatixConfig.Builder builder = languageCompilerInputBuilder.withStatix();
            subParts.getOneSubterm("StatixSource").ifSome(source -> {
                if(TermUtils.isAppl(source, "StatixFiles", 1)) {
                    final Parts filesParts = subParts.subParts(source.getSubterm(0));
                    final CfgStatixSource.Files.Builder filesSourceBuilder = CfgStatixSource.Files.builder().compileLanguageShared(languageShared);
                    final ResourcePath mainSourceDirectory = filesParts.getOneSubtermAsExistingDirectory("StatixFilesMainSourceDirectory", rootDirectory, "Statix main source directory")
                        .unwrapOrElse(() -> CfgStatixSource.Files.Builder.getDefaultMainSourceDirectory(languageShared));
                    filesSourceBuilder.mainSourceDirectory(mainSourceDirectory);
                    filesParts.forOneSubtermAsExistingFile("StatixFilesMainFile", mainSourceDirectory, "Statix main file", filesSourceBuilder::mainFile);
                    filesParts.forAllSubtermsAsExistingDirectories("StatixFilesIncludeDirectory", rootDirectory, "Statix include directory", filesSourceBuilder::addIncludeDirectories);
                    builder.source(CfgStatixSource.files(filesSourceBuilder.build()));
                } else if(TermUtils.isAppl(source, "StatixPrebuilt", 1)) {
                    final Parts prebuiltParts = subParts.subParts(source.getSubterm(0));
                    prebuiltParts.getOneSubtermAsExistingDirectory("StatixPrebuiltSpecAtermDirectory", rootDirectory, "Statix prebuilt spec ATerm directory").ifElse(
                        dir -> builder.source(CfgStatixSource.prebuilt(dir)),
                        () -> messagesBuilder.addMessage("spec-aterm-directory = $Path option is missing", Severity.Error, cfgFile, TermTracer.getRegion(source))
                    );
                } else {
                    throw new InvalidAstShapeException("Statix source", source);
                }
            });
            subParts.forOneSubtermAsBool("StatixSdf3SignatureGen", builder::enableSdf3SignatureGen);
        });
        parts.getAllSubTermsInListAsParts("DynamixSection").ifSome(subParts -> {
            final CfgDynamixConfig.Builder builder = languageCompilerInputBuilder.withDynamix();
            subParts.getOneSubterm("DynamixSource").ifSome(source -> {
                if(TermUtils.isAppl(source, "DynamixFiles", 1)) {
                    final Parts filesParts = subParts.subParts(source.getSubterm(0));
                    final CfgDynamixSource.Files.Builder filesSourceBuilder = CfgDynamixSource.Files.builder().compileLanguageShared(languageShared);
                    final ResourcePath mainSourceDirectory = filesParts.getOneSubtermAsExistingDirectory("DynamixFilesMainSourceDirectory", rootDirectory, "Dynamix main source directory")
                        .unwrapOrElse(() -> CfgDynamixSource.Files.Builder.getDefaultMainSourceDirectory(languageShared));
                    filesSourceBuilder.mainSourceDirectory(mainSourceDirectory);
                    filesParts.forOneSubtermAsExistingFile("DynamixFilesMainFile", mainSourceDirectory, "Dynamix main file", filesSourceBuilder::mainFile);
                    builder.source(CfgDynamixSource.files(filesSourceBuilder.build()));
                } else if(TermUtils.isAppl(source, "DynamixPrebuilt", 1)) {
                    final Parts prebuiltParts = subParts.subParts(source.getSubterm(0));
                    prebuiltParts.getOneSubtermAsExistingDirectory("DynamixPrebuiltSpecAtermDirectory", rootDirectory, "Dynamix prebuilt spec ATerm directory").ifElse(
                        dir -> builder.source(CfgDynamixSource.prebuilt(dir)),
                        () -> messagesBuilder.addMessage("spec-aterm-directory = $Path option is missing", Severity.Error, cfgFile, TermTracer.getRegion(source))
                    );
                } else {
                    throw new InvalidAstShapeException("Dynamix source", source);
                }
            });
        });
        parts.getAllSubTermsInListAsParts("StrategoSection").ifSome(subParts -> {
            final CfgStrategoConfig.Builder builder = languageCompilerInputBuilder.withStratego();
            subParts.getOneSubterm("StrategoSource").ifSome(source -> {
                if(TermUtils.isAppl(source, "StrategoFiles", 1)) {
                    final Parts filesParts = subParts.subParts(source.getSubterm(0));
                    final CfgStrategoSource.Files.Builder filesSourceBuilder = CfgStrategoSource.Files.builder().compileLanguageShared(languageShared);
                    final ResourcePath mainSourceDirectory = filesParts.getOneSubtermAsExistingDirectory("StrategoFilesMainSourceDirectory", rootDirectory, "Stratego main source directory")
                        .unwrapOrElse(() -> CfgStrategoSource.Files.Builder.getDefaultMainSourceDirectory(languageShared));
                    filesSourceBuilder.mainSourceDirectory(mainSourceDirectory);
                    filesParts.forOneSubtermAsExistingFile("StrategoFilesMainFile", mainSourceDirectory, "Stratego main file", filesSourceBuilder::mainFile);
                    filesParts.forAllSubtermsAsExistingDirectories("StrategoFilesIncludeDirectory", rootDirectory, "Stratego include directory", filesSourceBuilder::addIncludeDirectories);
                    builder.source(CfgStrategoSource.files(filesSourceBuilder.build()));
                } else {
                    throw new InvalidAstShapeException("Stratego source", source);
                }
            });
            subParts.forOneSubtermAsBool("StrategoSdf3StatixExplicationGen", builder::enableSdf3StatixExplicationGen);
            subParts.forOneSubtermAsString("StrategoLanguageStrategyAffix", builder::languageStrategyAffix);
            subParts.forOneSubtermAsString("StrategoOutputJavaPackageId", builder::outputJavaPackageId);
        });
        parts.getAllSubTermsInListAsParts("Dependencies").ifSome(subParts -> {
            subParts.forAll("Dependency", 2, dependencyTerm -> {
                final IStrategoAppl exprTerm = TermUtils.asApplAt(dependencyTerm, 0)
                    .orElseThrow(() -> new InvalidAstShapeException("constructor application as first subterm", dependencyTerm));
                final DependencySource dependencySource;
                switch(exprTerm.getConstructor().getName()) {
                    case "String": {
                        final String id = TermUtils.asJavaStringAt(exprTerm, 0)
                            .orElseThrow(() -> new InvalidAstShapeException("string as first subterm", exprTerm));
                        dependencySource = DependencySource.coordinateRequirement(new CoordinateRequirement(id));
                        break;
                    }
                    case "CoordinateRequirement": {
                        final String groupString = TermUtils.asJavaStringAt(exprTerm, 0)
                            .orElseThrow(() -> new InvalidAstShapeException("string as first subterm", exprTerm));
                        final String id = TermUtils.asJavaStringAt(exprTerm, 1)
                            .orElseThrow(() -> new InvalidAstShapeException("string as second subterm", exprTerm));
                        final String versionString = TermUtils.asJavaStringAt(exprTerm, 2)
                            .orElseThrow(() -> new InvalidAstShapeException("string as third subterm", exprTerm));
                        dependencySource = DependencySource.coordinateRequirement(new CoordinateRequirement(
                            groupString.equals("*") ? null : groupString,
                            id,
                            versionString.equals("*") ? null : Version.parse(versionString)
                        ));
                        break;
                    }
                    case "Coordinate": {
                        final String groupId = TermUtils.asJavaStringAt(exprTerm, 0)
                            .orElseThrow(() -> new InvalidAstShapeException("string as first subterm", exprTerm));
                        final String id = TermUtils.asJavaStringAt(exprTerm, 1)
                            .orElseThrow(() -> new InvalidAstShapeException("string as second subterm", exprTerm));
                        final String version = TermUtils.asJavaStringAt(exprTerm, 2)
                            .orElseThrow(() -> new InvalidAstShapeException("string as third subterm", exprTerm));
                        dependencySource = DependencySource.coordinate(new Coordinate(
                            groupId,
                            id,
                            Version.parse(version)
                        ));
                        break;
                    }
                    case "Path": {
                        final String path = TermUtils.asJavaStringAt(exprTerm, 0)
                            .orElseThrow(() -> new InvalidAstShapeException("string as first subterm", exprTerm));
                        dependencySource = DependencySource.path(path);
                        break;
                    }
                    default:
                        throw new InvalidAstShapeException("Dependency expression", exprTerm);
                }
                final LinkedHashSet<DependencyKind> dependencyKinds = new LinkedHashSet<>();
                final IStrategoAppl kindTerm = TermUtils.asApplAt(dependencyTerm, 1) // TODO: go over list once this becomes a list of kinds.
                    .orElseThrow(() -> new InvalidAstShapeException("constructor application as second subterm", dependencyTerm));
                switch(kindTerm.getConstructor().getName()) {
                    case "CompileTimeDependency":
                        dependencyKinds.add(DependencyKind.CompileTime);
                        break;
                    case "RunTimeDependency":
                        dependencyKinds.add(DependencyKind.RunTime);
                        break;
                    default:
                        throw new InvalidAstShapeException("Dependency kind", kindTerm);
                }
                languageCompilerInputBuilder.compileLanguage.addDependencies(new Dependency(dependencySource, SetView.of(dependencyKinds)));
            });
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
                } else if(TermUtils.isAppl(variant, "Jsglr2", 1)) {
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
                } else {
                    throw new InvalidAstShapeException("JSGLR parser variant", variant);
                }
            });
            // TODO: parser language properties
            final ParserAdapterCompiler.Input.Builder adapter = adapterBuilder.withParser();
            // TODO: parser adapter properties
        });
        parts.getAllSubTermsInListAsParts("CommentSymbolSection").ifSome(subParts -> {
            subParts.forAllSubtermsAsStrings("LineComment", adapterBuilder.project::addLineCommentSymbols);
            subParts.forAll("BlockComment", 2, blockCommentTerm -> {
                final String open = Parts.toJavaString(blockCommentTerm.getSubterm(0));
                final String close = Parts.toJavaString(blockCommentTerm.getSubterm(1));
                adapterBuilder.project.addBlockCommentSymbols(new BlockCommentSymbols(open, close));
            });
        });
        parts.getAllSubTermsInListAsParts("BracketSymbolSection").ifSome(subParts -> {
            subParts.forAll("Bracket", 2, bracketTerm -> {
                final char open = Parts.toJavaChar(bracketTerm.getSubterm(0));
                final char close = Parts.toJavaChar(bracketTerm.getSubterm(1));
                adapterBuilder.project.addBracketSymbols(new BracketSymbols(open, close));
            });
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
            subParts.forOneSubtermAsInt("ConstraintAnalyzerDefaultStatixMessageStacktraceLength", adapter::defaultStatixMessageStacktraceLength);
            subParts.forOneSubtermAsInt("ConstraintAnalyzerDefaultStatixMessageTermDepth", adapter::defaultStatixMessageTermDepth);
            subParts.forOneSubtermAsString("ConstraintAnalyzerDefaultStatixTestLogLevel", adapter::defaultStatixTestLogLevel);
            subParts.forOneSubtermAsBool("ConstraintAnalyzerDefaultStatixSuppressCascadingErrors", adapter::defaultStatixSuppressCascadingErrors);
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
        parts.getAllSubTermsInListAsParts("TegoRuntimeSection").ifSome(subParts -> {
            final TegoRuntimeAdapterCompiler.Input.Builder adapter = adapterBuilder.withTegoRuntime();
        });
        parts.getAllSubTermsInListAsParts("CodeCompletionSection").ifSome(subParts -> {
            final CodeCompletionAdapterCompiler.Input.Builder adapter = adapterBuilder.withCodeCompletion();
        });
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
            getCommandDefTaskType(commandDefParts)
                .ifNone(() -> messagesBuilder.addMessage("display-name = $String option is missing", Severity.Error, cfgFile, TermTracer.getRegion(commandDefTerm)))
                .ifSome(taskDefType -> {
                    commandDefBuilder.taskDefType(taskDefType);
                    commandDefBuilder.type(getCommandDefType(commandDefParts, languageAdapterShared, taskDefType));
                    getCommandDefDisplayName(commandDefParts)
                        .ifNone(() -> messagesBuilder.addMessage("task-def = $TaskDef option is missing", Severity.Error, cfgFile, TermTracer.getRegion(commandDefTerm)))
                        .ifSome(displayName -> {
                            commandDefBuilder.displayName(displayName);
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
                                    final Parts parameterParts = new Parts(context, messagesBuilder, cfgFile, parameterProperties);
                                    parameterParts.forOneSubtermAsTypeInfo("ParameterType", parameterBuilder::type);
                                    parameterParts.forOneSubtermAsBool("ParameterRequired", parameterBuilder::required);
                                    parameterParts.getAllSubTermsInList("ParameterArgumentProviders").forEach(parameterArgumentProviderTerm -> { // NOTE: not using getAllSubTermsInListAsParts because order matters here.
                                        parameterBuilder.addProviders(toParameterArgumentProvider(parameterArgumentProviderTerm));
                                    });
                                    commandDefBuilder.addParams(parameterBuilder.build());
                                });
                            });
                        });
                    adapterBuilder.project.addCommandDefs(commandDefBuilder.build());
                });
        }

        // TODO: adapt to new AST shape, set sane defaults, and ensure that menus are merged!
        // LanguageAdapterCompilerInput > Menus
        parts.forAllSubTermsInList("MainMenu", menuItem -> {
            toMenuItemRepr(messagesBuilder, cfgFile, parts, menuItem, languageAdapterShared)
                .ifSome(menuItemRepr -> adapterBuilder.project.addMainMenuItems(menuItemRepr));
        });
        parts.forAllSubTermsInList("ResourceContextMenu", menuItem -> {
            toMenuItemRepr(messagesBuilder, cfgFile, parts, menuItem, languageAdapterShared)
                .ifSome(menuItemRepr -> adapterBuilder.project.addResourceContextMenuItems(menuItemRepr));
        });
        parts.forAllSubTermsInList("EditorContextMenu", menuItem -> {
            toMenuItemRepr(messagesBuilder, cfgFile, parts, menuItem, languageAdapterShared)
                .ifSome(menuItemRepr -> adapterBuilder.project.addEditorContextMenuItems(menuItemRepr));
        });

        // LanguageAdapterCompilerInput > dependencies
        if(parts.getOneSubtermAsBool("DependOnRv32Im").unwrapOr(false)) {
            adapterBuilder.project.dependOnRv32Im(true);
        }

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
            subParts.forOneSubtermAsTypeInfo("EclipseBaseParticipant", builder::baseParticipant);
            subParts.forOneSubtermAsTypeInfo("EclipseExtendParticipant", builder::extendParticipant);
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

    private static Option<TypeInfo> getCommandDefTaskType(Parts commandDefParts) {
        return commandDefParts.getOneSubterm("CommandDefTaskDef").map(CfgAstToObject::taskDefToTypeInfo);
    }

    private static TypeInfo getCommandDefType(Parts commandDefParts, AdapterProject languageAdapterShared, TypeInfo taskDefType) {
        // Get type from command definition, or default to: <command package>.<task definition name>Command.
        return commandDefParts.getOneSubtermAsTypeInfo("CommandDefType").unwrapOrElse(() -> TypeInfo.of(languageAdapterShared.commandPackageId(), taskDefType.id() + "Command"));
    }

    private static Option<String> getCommandDefDisplayName(Parts commandDefParts) {
        return commandDefParts.getOneSubtermAsString("CommandDefDisplayName");
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

    private static Option<MenuItemRepr> toMenuItemRepr(KeyedMessagesBuilder messagesBuilder, @Nullable ResourceKey cfgFile, Parts mainParts, IStrategoTerm menuItem, AdapterProject languageAdapterShared) {
        final IStrategoTerm actualMenuItem = menuItem.getSubterm(0); // Menu item expressions are always wrapped in a MenuItem(...) term.
        final IStrategoAppl appl = TermUtils.asAppl(actualMenuItem).orElseThrow(() -> new InvalidAstShapeException("a term application", actualMenuItem));
        switch(appl.getConstructor().getName()) {
            case "Separator":
                return Option.ofSome(MenuItemRepr.separator());
            case "Menu": {
                final String displayName = Parts.toJavaString(appl.getSubterm(0));
                final IStrategoList subMenuItemsTerm = TermUtils.asListAt(appl, 1)
                    .orElseThrow(() -> new InvalidAstShapeException("a list of sub-menu items as second subterm", appl));
                return Option.transpose(subMenuItemsTerm.getSubterms().stream().map(t -> toMenuItemRepr(messagesBuilder, cfgFile, mainParts, t, languageAdapterShared)).collect(Collectors.toList()))
                    .map(subMenuItems -> MenuItemRepr.menu(displayName, subMenuItems));
            }
            case "CommandAction": {
                final IStrategoList properties = TermUtils.asListAt(appl, 0)
                    .orElseThrow(() -> new InvalidAstShapeException("a list of command action properties as first subterm", appl));
                final Parts commandActionParts = mainParts.subParts(properties);
                final CommandActionRepr.Builder commandActionBuilder = CommandActionRepr.builder();

                return commandActionParts.getOneSubterm("CommandActionDef")
                    .ifNone(() -> messagesBuilder.addMessage("command-def = $CommandDef option is missing", Severity.Error, cfgFile, TermTracer.getRegion(appl)))
                    .map(CfgAstToObject::getActualCommandDefTerm)
                    .flatMap(commandDefTerm -> {
                        final Parts commandDefParts = mainParts.subParts(commandDefTerm.getSubterm(0));
                        return getCommandDefTaskType(commandDefParts)
                            .ifNone(() -> messagesBuilder.addMessage("task-def = $TaskDef option is missing", Severity.Error, cfgFile, TermTracer.getRegion(commandDefTerm)))
                            .flatMap(commandDefTaskType -> {
                                final CommandRequestRepr.Builder commandRequestBuilder = CommandRequestRepr.builder();
                                final TypeInfo commandDefType = getCommandDefType(commandDefParts, languageAdapterShared, commandDefTaskType);
                                commandRequestBuilder.commandDefType(commandDefType);
                                return commandActionParts.getOneSubterm("CommandActionExecutionType")
                                    .ifNone(() -> messagesBuilder.addMessage("execution-type = $ExecutionType option is missing", Severity.Error, cfgFile, TermTracer.getRegion(appl)))
                                    .map(CfgAstToObject::toCommandExecutionType)
                                    .flatMap(commandExecutionType -> {
                                        commandRequestBuilder.executionType(commandExecutionType);
                                        // TODO: initial arguments
                                        commandActionBuilder.commandRequest(commandRequestBuilder.build());
                                        return getCommandDefDisplayName(commandDefParts)
                                            .ifNone(() -> messagesBuilder.addMessage("display-name = $String option is missing", Severity.Error, cfgFile, TermTracer.getRegion(commandDefTerm)))
                                            .map(commandDefDisplayName -> {
                                                final String displayName = commandActionParts.getOneSubtermAsString("CommandActionDisplayName")
                                                    .unwrapOrElse(() -> commandDefDisplayName + (commandExecutionType == CommandExecutionType.ManualContinuous ? " (continuous)" : ""));
                                                commandActionBuilder.displayName(displayName);
                                                commandActionParts.getOneSubtermAsString("CommandActionDescription").orElse(() -> getCommandDefDescription(commandDefParts)).ifSome(commandActionBuilder::description);
                                                commandActionParts.forAllSubTermsInList("CommandActionRequiredEditorSelectionTypes", term -> commandActionBuilder.addRequiredEditorSelectionTypes(toEditorSelectionType(term)));
                                                commandActionParts.forAllSubTermsInList("CommandActionRequiredEditorFileTypes", term -> commandActionBuilder.addRequiredEditorFileTypes(toEditorFileType(term)));
                                                commandActionParts.forAllSubTermsInList("CommandActionRequiredHierarchicalResourceTypes", term -> commandActionBuilder.addRequiredResourceTypes(toHierarchicalResourceType(term)));
                                                commandActionParts.forAllSubTermsInList("CommandActionRequiredEnclosingResourceTypes", term -> commandActionBuilder.addRequiredEnclosingResourceTypes(toEnclosingCommandContextType(term)));
                                                return MenuItemRepr.commandAction(commandActionBuilder.build());
                                            });
                                    });
                            });
                    });
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

