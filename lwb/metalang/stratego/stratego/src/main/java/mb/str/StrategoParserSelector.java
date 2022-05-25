package mb.str;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.MapView;
import mb.jsglr2.common.Jsglr2ParseTable;
import mb.jsglr2.common.Jsglr2ParseTableException;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.str.task.spoofax.StrategoAnalyzeConfigFunctionWrapper;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.parsetable.IParseTable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

@StrategoScope
public class StrategoParserSelector {
    private static final String commentMarker = "//!";

    private final Provider<StrategoParser> defaultParserProvider;
    private final StrategoAnalyzeConfigFunctionWrapper configFunctionWrapper;
    private final MapView<String, StrategoParseTable> builtinAlternativeParseTables;


    @Inject public StrategoParserSelector(
        Provider<StrategoParser> defaultParserProvider,
        StrategoAnalyzeConfigFunctionWrapper configFunctionWrapper,
        @StrategoQualifier("definition-directory") HierarchicalResource definitionDir
    ) {
        this.defaultParserProvider = defaultParserProvider;
        this.configFunctionWrapper = configFunctionWrapper;
        this.builtinAlternativeParseTables = loadBuiltinAlternativeParseTables(definitionDir);
    }

    private static MapView<String, StrategoParseTable> loadBuiltinAlternativeParseTables(HierarchicalResource definitionDir) {
        final HashMap<String, StrategoParseTable> parseTables = new HashMap<>();
        return MapView.of(parseTables);
    }

    private static StrategoParseTable loadParseTable(HierarchicalResource definitionDir, String name) {
        final HierarchicalResource atermFile = definitionDir.appendRelativePath(name + ".tbl");
        try(final InputStream atermInputStream = atermFile.openRead()) {
            final Jsglr2ParseTable parseTable = Jsglr2ParseTable.fromStream(atermInputStream);
            return new StrategoParseTable(parseTable);
        } catch(Jsglr2ParseTableException | IOException e) {
            throw new RuntimeException("Cannot create parse table; cannot read parse table from resource '" + atermFile + "' in classloader resources", e);
        }
    }


    public Result<Provider<StrategoParser>, ?> getParserProvider(
        ExecContext context,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint
    ) throws IOException, InterruptedException {
        final @Nullable MapView<String, Supplier<Result<IParseTable, ?>>> alternativeParseTables;
        if(rootDirectoryHint != null) {
            alternativeParseTables = configFunctionWrapper.get()
                .apply(context, rootDirectoryHint)
                .map(o -> o.map(c -> c.alternativeParseTables))
                .mapOrElse(Option::get, () -> null);
        } else {
            alternativeParseTables = null;
        }

        @Nullable String parseTableId = null;
        if(fileHint != null) {
            parseTableId = getAlternativeParseTableIdFromFileComment(context, fileHint);
            if(parseTableId == null && fileHint instanceof ResourcePath) {
                parseTableId = getAlternativeParseTableIdFromMetaFile(context, (ResourcePath)fileHint);
            }
        }

        try {
            return Result.ofOk(provideParser(context, parseTableId, alternativeParseTables));
        } catch(ParseError e) { // NOTE: ParseError is a RuntimeException, but really indicates an error.
            return Result.ofErr(e);
        } catch(RuntimeException | InterruptedException e) {
            throw e;
        } catch(Exception e) {
            return Result.ofErr(e);
        }
    }

    private @Nullable String getAlternativeParseTableIdFromFileComment(
        ExecContext context,
        ResourceKey filePath
    ) throws IOException {
        final ReadableResource file = context.require(filePath);
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(file.openRead()))) {
            @Nullable String firstLine = reader.readLine();
            if(firstLine != null) {
                firstLine = firstLine.trim();
                if(firstLine.startsWith(commentMarker)) {
                    return firstLine.replace(commentMarker, "").trim();
                }
            }
        }
        return null;
    }

    private @Nullable String getAlternativeParseTableIdFromMetaFile(
        ExecContext context,
        ResourcePath filePath
    ) throws IOException, ParseError {
        final @Nullable String leafWithoutExtension = filePath.getLeafWithoutFileExtension();
        if(leafWithoutExtension == null) return null;
        final @Nullable ResourcePath parentPath = filePath.getParent();
        if(parentPath == null) return null;
        final ResourcePath metaFilePath = parentPath.appendRelativePath(leafWithoutExtension + ".meta");
        final ReadableResource metaFile = context.require(metaFilePath);
        if(!metaFile.exists() || !metaFile.isReadable()) return null;
        final ITermFactory termFactory = new TermFactory();
        final TermReader reader = new TermReader(termFactory);
        final IStrategoTerm metaTerm;
        try(final BufferedInputStream inputStream = metaFile.openReadBuffered()) {
            metaTerm = reader.parseFromStream(inputStream);
        }
        if(metaTerm.getSubtermCount() < 1) return null; // TODO: errors should be shown on .meta file
        for(IStrategoTerm entry : metaTerm.getSubterm(0).getAllSubterms()) {
            if(!(entry instanceof IStrategoAppl)) continue;
            final String cons = ((IStrategoAppl)entry).getConstructor().getName();
            if(cons.equals("Syntax")) {
                return TermUtils.asJavaStringAt(entry, 0)
                    .orElse(null); // TODO: errors should be shown on .meta file
            }
        }
        return null;
    }

    private Provider<StrategoParser> provideParser(
        ExecContext context,
        @Nullable String parseTableId,
        @Nullable MapView<String, Supplier<Result<IParseTable, ?>>> alternativeParseTables
    ) throws Exception {
        if(parseTableId == null) return defaultParserProvider;
        final @Nullable Supplier<Result<StrategoParseTable, ?>> parseTableSupplier = getParseTableSupplier(parseTableId, alternativeParseTables);
        if(parseTableSupplier == null) return defaultParserProvider;
        final StrategoParseTable parseTable = context.require(parseTableSupplier).unwrap();
        return () -> new StrategoParser(parseTable);
    }

    private @Nullable Supplier<Result<StrategoParseTable, ?>> getParseTableSupplier(
        String parseTableId,
        @Nullable MapView<String, Supplier<Result<IParseTable, ?>>> alternativeParseTables
    ) {
        if(alternativeParseTables != null) {
            final @Nullable Supplier<Result<IParseTable, ?>> table = alternativeParseTables.get(parseTableId);
            if(table != null) {
                return table.map(r -> r.map(t -> new StrategoParseTable(new Jsglr2ParseTable(t))));
            }
        }
        final @Nullable StrategoParseTable builtinTable = builtinAlternativeParseTables.get(parseTableId);
        if(builtinTable != null) {
            return new ValueSupplier<>(Result.ofOk(builtinTable));
        }
        return null;
    }
}
