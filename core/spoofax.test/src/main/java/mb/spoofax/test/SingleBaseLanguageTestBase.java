package mb.spoofax.test;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.MapView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseInput;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.log.api.LoggerFactory;
import mb.log.dagger.LoggerComponent;
import mb.resource.DefaultResourceService;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.url.URLResourceRegistry;
import mb.spoofax.compiler.interfaces.spoofaxcore.Parser;
import mb.spoofax.compiler.interfaces.spoofaxcore.Styler;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SingleBaseLanguageTestBase extends TestBase {
    public final ResourceService resourceService;
    public final HierarchicalResource definitionDirectory;

    public final Parser parser;
    public final String startSymbol;

    public final Styler styler;

    public final StrategoRuntimeBuilder strategoRuntimeBuilder;
    public final StrategoRuntime strategoRuntime;

    public final ConstraintAnalyzer analyzer;
    public final boolean multiFileAnalysis;

    public final TermFactory termFactory = new TermFactory();


    @FunctionalInterface
    public interface StrategoRuntimeBuilderFactory {
        StrategoRuntimeBuilder create(
            LoggerFactory loggerFactory,
            ResourceService resourceService,
            HierarchicalResource definitionDirectory
        );
    }

    public SingleBaseLanguageTestBase(
        LoggerComponent loggerComponent,
        Supplier<ResourceRegistry> classLoaderResourceRegistrySupplier,
        Supplier<HierarchicalResource> definitionDirSupplier,
        BiFunction<LoggerFactory, HierarchicalResource, Parser> parserFunction,
        String startSymbol,
        BiFunction<LoggerFactory, HierarchicalResource, Styler> stylerFunction,
        StrategoRuntimeBuilderFactory strategoRuntimeBuilderFactory,
        Function<ResourceService, ConstraintAnalyzer> analyzerFunction,
        boolean multiFileAnalysis
    ) {
        super(loggerComponent);
        this.resourceService = new DefaultResourceService(new FSResourceRegistry(), new URLResourceRegistry(), classLoaderResourceRegistrySupplier.get());
        this.definitionDirectory = definitionDirSupplier.get();
        this.parser = parserFunction.apply(loggerFactory, definitionDirectory);
        this.startSymbol = startSymbol;
        this.styler = stylerFunction.apply(loggerFactory, definitionDirectory);
        this.strategoRuntimeBuilder = strategoRuntimeBuilderFactory.create(loggerFactory, resourceService, definitionDirectory);
        this.strategoRuntime = strategoRuntimeBuilder.build();
        this.analyzer = analyzerFunction.apply(resourceService);
        this.multiFileAnalysis = multiFileAnalysis;
    }


    public JSGLR1ParseOutput parse(String text, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) throws JSGLR1ParseException, InterruptedException {
        return parser.parse(new JSGLR1ParseInput(text, startSymbol, fileHint, rootDirectoryHint));
    }

    public JSGLR1ParseOutput parse(String text, @Nullable ResourceKey fileHint) throws JSGLR1ParseException, InterruptedException {
        return parse(text, fileHint, rootPath);
    }

    public JSGLR1ParseOutput parse(String text) throws JSGLR1ParseException, InterruptedException {
        return parse(text, null);
    }

    public JSGLR1ParseOutput parse(ReadableResource file) throws JSGLR1ParseException, InterruptedException, IOException {
        return parse(file.readString(), file.getKey());
    }


    public Styling style(Iterable<? extends Token<IStrategoTerm>> tokens) {
        return styler.style(tokens);
    }


    public ConstraintAnalyzerContext constraintAnalyzerContext(boolean multiFile, @Nullable ResourceKey root) {
        return new ConstraintAnalyzerContext(multiFileAnalysis, root);
    }

    public ConstraintAnalyzerContext defaultConstraintAnalyzerContext() {
        return constraintAnalyzerContext(multiFileAnalysis, multiFileAnalysis ? rootPath : null);
    }


    public ConstraintAnalyzer.SingleFileResult analyze(
        @Nullable ResourcePath root,
        ResourceKey resource,
        IStrategoTerm ast,
        ConstraintAnalyzerContext context
    ) throws ConstraintAnalyzerException {
        return analyzer.analyze(root, resource, ast, context, strategoRuntime, resourceService);
    }

    public ConstraintAnalyzer.SingleFileResult analyze(
        @Nullable ResourcePath root,
        ReadableResource resource,
        IStrategoTerm ast,
        ConstraintAnalyzerContext context
    ) throws ConstraintAnalyzerException {
        return analyzer.analyze(root, resource.getKey(), ast, context, strategoRuntime, resourceService);
    }

    public ConstraintAnalyzer.SingleFileResult analyze(
        ResourceKey resource,
        IStrategoTerm ast
    ) throws ConstraintAnalyzerException {
        return analyzer.analyze(multiFileAnalysis ? rootPath : null, resource, ast, defaultConstraintAnalyzerContext(), strategoRuntime, resourceService);
    }

    public ConstraintAnalyzer.SingleFileResult analyze(
        ReadableResource resource,
        IStrategoTerm ast
    ) throws ConstraintAnalyzerException {
        return analyzer.analyze(multiFileAnalysis ? rootPath : null, resource.getKey(), ast, defaultConstraintAnalyzerContext(), strategoRuntime, resourceService);
    }

    public ConstraintAnalyzer.MultiFileResult analyze(
        @Nullable ResourcePath root,
        MapView<ResourceKey, IStrategoTerm> asts,
        ConstraintAnalyzerContext context
    ) throws ConstraintAnalyzerException {
        return analyzer.analyze(root, asts, context, strategoRuntime, resourceService);
    }

    public ConstraintAnalyzer.MultiFileResult analyze(
        MapView<ResourceKey, IStrategoTerm> asts
    ) throws ConstraintAnalyzerException {
        return analyzer.analyze(multiFileAnalysis ? rootPath : null, asts, defaultConstraintAnalyzerContext(), strategoRuntime, resourceService);
    }
}
