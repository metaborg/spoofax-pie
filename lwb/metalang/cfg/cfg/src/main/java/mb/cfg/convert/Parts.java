package mb.cfg.convert;

import mb.aterm.common.InvalidAstShapeException;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.MultiMap;
import mb.common.util.StreamIterable;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.TypeInfo;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

class Parts {
    private final ExecContext context;
    private final KeyedMessagesBuilder messagesBuilder;
    private final @Nullable ResourceKey cfgFile;
    private final MultiMap<IStrategoConstructor, IStrategoAppl> parts;
    private final TermFactory termFactory = new TermFactory();

    Parts(
        ExecContext context,
        KeyedMessagesBuilder messagesBuilder,
        @Nullable ResourceKey cfgFile,
        Iterable<IStrategoTerm> terms
    ) throws InvalidAstShapeException {
        this.context = context;
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
        ExecContext context,
        KeyedMessagesBuilder messagesBuilder,
        @Nullable ResourceKey cfgFile,
        Stream<IStrategoTerm> terms
    ) throws InvalidAstShapeException {
        this(context, messagesBuilder, cfgFile, new StreamIterable<>(terms));
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


    Option<IStrategoAppl> getOne(IStrategoConstructor constructor) {
        final ArrayList<IStrategoAppl> values = parts.get(constructor);
        if(values.isEmpty()) {
            return Option.ofNone();
        } else if(values.size() == 1) {
            return Option.ofSome(values.get(0));
        } else {
            @MonotonicNonNull IStrategoAppl first = null;
            for(IStrategoAppl part : values) {
                if(first == null) {
                    first = part;
                } else {
                    createCfgWarning("Option ignored because it was defined before", part);
                }
            }
            return Option.ofSome(first);
        }
    }

    void forOne(IStrategoConstructor constructor, Consumer<IStrategoAppl> consumer) {
        getOne(constructor).ifSome(consumer);
    }

    Option<IStrategoAppl> getOne(String name, int arity) {
        return getOne(termFactory.makeConstructor(name, arity));
    }

    void forOne(String name, int arity, Consumer<IStrategoAppl> consumer) {
        getOne(name, arity).ifSome(consumer);
    }


    Option<IStrategoTerm> getOneSubterm(String name) {
        return getOne(name, 1).map(t -> t.getSubterm(0));
    }

    void forOneSubterm(String name, Consumer<IStrategoTerm> consumer) {
        getOneSubterm(name).ifSome(consumer);
    }

    Option<Boolean> getOneSubtermAsBool(String name) {
        return getOneSubterm(name)
            .flatMap(t -> Option.ofOptional(TermUtils.asApplAt(t, 0)))
            .map(a -> a.getConstructor().getName().equals("True"));
    }

    void forOneSubtermAsBool(String name, Consumer<Boolean> consumer) {
        getOneSubtermAsBool(name).ifSome(consumer);
    }

    Option<Integer> getOneSubtermAsInt(String name) {
        return getOneSubterm(name).map(Parts::toJavaInt);
    }

    void forOneSubtermAsInt(String name, Consumer<Integer> consumer) {
        getOneSubtermAsInt(name).ifSome(consumer);
    }

    Option<String> getOneSubtermAsString(String name) {
        return getOneSubterm(name).map(Parts::toJavaString);
    }

    void forOneSubtermAsString(String name, Consumer<String> consumer) {
        getOneSubtermAsString(name).ifSome(consumer);
    }

    Option<ResourcePath> getOneSubtermAsPath(String name, ResourcePath base) {
        return getOneSubtermAsString(name).map(base::appendRelativePath).map(ResourcePath::getNormalized);
    }

    void forOneSubtermAsPath(String name, ResourcePath base, Consumer<ResourcePath> consumer) {
        getOneSubtermAsPath(name, base).ifSome(consumer);
    }

    Option<ResourcePath> getOneSubtermAsExistingFile(String name, ResourcePath base, String errorSuffix) {
        return getOneSubterm(name).map(t -> pathAsExistingFile(t, base, errorSuffix));
    }

    void forOneSubtermAsExistingFile(String name, ResourcePath base, String errorSuffix, Consumer<ResourcePath> consumer) {
        getOneSubtermAsExistingFile(name, base, errorSuffix).ifSome(consumer);
    }

    Option<ResourcePath> getOneSubtermAsExistingDirectory(String name, ResourcePath base, String errorSuffix) {
        return getOneSubterm(name).map(t -> pathAsExistingDirectory(t, base, errorSuffix));
    }

    void forOneSubtermAsExistingDirectory(String name, ResourcePath base, String errorSuffix, Consumer<ResourcePath> consumer) {
        getOneSubtermAsExistingDirectory(name, base, errorSuffix).ifSome(consumer);
    }

    Option<TypeInfo> getOneSubtermAsTypeInfo(String name) {
        return getOneSubtermAsString(name).map(TypeInfo::of);
    }

    void forOneSubtermAsTypeInfo(String name, Consumer<TypeInfo> consumer) {
        getOneSubtermAsTypeInfo(name).ifSome(consumer);
    }

    Option<String> getOneSubtermAsConstructorName(String name) {
        return getOneSubterm(name).map(IStrategoAppl.class::cast).map(IStrategoAppl::getName);
    }

    void forOneSubtermAsConstructorName(String name, Consumer<String> consumer) {
        getOneSubtermAsConstructorName(name).ifSome(consumer);
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

    Stream<ResourcePath> getAllSubTermsAsPaths(String name, ResourcePath base) {
        return getAllSubTermsAsStrings(name).map(base::appendRelativePath).map(ResourcePath::getNormalized);
    }

    void forAllSubtermsAsPaths(String name, ResourcePath base, Consumer<ResourcePath> consumer) {
        getAllSubTermsAsPaths(name, base).forEach(consumer);
    }

    Stream<ResourcePath> getAllSubTermsAsExistingFiles(String name, ResourcePath base, String errorSuffix) {
        return getAllSubTerms(name).map(t -> pathAsExistingFile(t, base, errorSuffix));
    }

    void forAllSubtermsAsExistingFiles(String name, ResourcePath base, String errorSuffix, Consumer<ResourcePath> consumer) {
        getAllSubTermsAsExistingFiles(name, base, errorSuffix).forEach(consumer);
    }

    Stream<ResourcePath> getAllSubTermsAsExistingDirectories(String name, ResourcePath base, String errorSuffix) {
        return getAllSubTerms(name).map(t -> pathAsExistingDirectory(t, base, errorSuffix));
    }

    void forAllSubtermsAsExistingDirectories(String name, ResourcePath base, String errorSuffix, Consumer<ResourcePath> consumer) {
        getAllSubTermsAsExistingDirectories(name, base, errorSuffix).forEach(consumer);
    }

    Stream<TypeInfo> getAllSubtermsAsTypeInfo(String name) {
        return getAllSubTermsAsStrings(name).map(TypeInfo::of);
    }

    void forAllSubtermsAsTypeInfo(String name, Consumer<TypeInfo> consumer) {
        getAllSubtermsAsTypeInfo(name).forEach(consumer);
    }

    Option<Parts> getAllSubTermsAsParts(String name) {
        if(!contains(name)) return Option.ofNone();
        return Option.ofSome(new Parts(context, messagesBuilder, cfgFile, getAllSubTerms(name)));
    }


    Stream<IStrategoTerm> getAllSubTermsInList(String name) {
        return getAll(name, 1).stream().flatMap(t -> t.getSubterm(0).getSubterms().stream());
    }

    void forAllSubTermsInList(String name, Consumer<IStrategoTerm> consumer) {
        getAllSubTermsInList(name).forEach(consumer);
    }

    Option<Parts> getAllSubTermsInListAsParts(String name) {
        if(!contains(name)) return Option.ofNone();
        return Option.ofSome(new Parts(context, messagesBuilder, cfgFile, getAllSubTermsInList(name)));
    }


    Parts subParts(Iterable<IStrategoTerm> terms) {
        return new Parts(context, messagesBuilder, cfgFile, terms);
    }

    Parts subParts(Stream<IStrategoTerm> terms) {
        return new Parts(context, messagesBuilder, cfgFile, terms);
    }


    private @Nullable ResourceKey getCfgFile(IStrategoTerm term) {
        if(cfgFile != null) return cfgFile;
        return TermTracer.getResourceKey(term);
    }

    private void createCfgMessage(String text, Severity severity, IStrategoTerm term) {
        messagesBuilder.addMessage(text, severity, getCfgFile(term), getRegion(term));
    }

    private void createCfgMessage(String text, Throwable e, IStrategoTerm term) {
        messagesBuilder.addMessage(text, e, Severity.Error, getCfgFile(term), getRegion(term));
    }


    private void createCfgError(String text, IStrategoTerm term) {
        createCfgMessage(text, Severity.Error, term);
    }

    private void createCfgError(String text, Throwable e, IStrategoTerm term) {
        createCfgMessage(text, e, term);
    }

    private void createCfgWarning(String text, IStrategoTerm term) {
        createCfgMessage(text, Severity.Warning, term);
    }


    private ResourcePath pathAsExistingFile(IStrategoTerm pathTerm, ResourcePath base, String errorSuffix) {
        final String relativePath = Parts.toJavaString(pathTerm);
        final ResourcePath path = base.appendRelativePath(relativePath).getNormalized();
        try {
            final HierarchicalResource file = context.require(path, ResourceStampers.<HierarchicalResource>exists());
            if(!file.exists()) {
                createCfgError(errorSuffix + " '" + path + "' does not exist", pathTerm);
            } else if(!file.isFile()) {
                createCfgError(errorSuffix + " '" + path + "' is not a file", pathTerm);
            }
        } catch(IOException e) {
            createCfgError("Failed to check if " + errorSuffix + " '" + path + "' exists", e, pathTerm);
        }
        return path;
    }

    private ResourcePath pathAsExistingDirectory(IStrategoTerm pathTerm, ResourcePath base, String errorSuffix) {
        final String relativePath = Parts.toJavaString(pathTerm);
        final ResourcePath path = base.appendRelativePath(relativePath).getNormalized();
        try {
            final HierarchicalResource directory = context.require(path, ResourceStampers.<HierarchicalResource>exists());
            if(!directory.exists()) {
                createCfgError(errorSuffix + " '" + path + "' does not exist", pathTerm);
            } else if(!directory.isDirectory()) {
                createCfgError(errorSuffix + " '" + path + "' is not a directory", pathTerm);
            }
        } catch(IOException e) {
            createCfgError("Failed to check if " + errorSuffix + " '" + path + "' exists", e, pathTerm);
        }
        return path;
    }


    private static @Nullable Region getRegion(IStrategoTerm term) {
        return TermTracer.getRegion(term);
    }

    public static Integer toJavaInt(IStrategoTerm term) {
        if(TermUtils.isAppl(term, "Int", 1) || TermUtils.isAppl(term, "UInt", 1)) {
            final String intString = TermUtils.asJavaStringAt(term, 0)
                .orElseThrow(() -> new InvalidAstShapeException("string term representing an integer as first subterm", term));
            return parseStringToInt(intString, term.getSubterm(0));
        } else if(TermUtils.isString(term)) {
            final String intString = TermUtils.asJavaStringAt(term, 0)
                .orElseThrow(() -> new InvalidAstShapeException("string term representing an integer", term));
            return parseStringToInt(intString, term);
        } else if(TermUtils.isInt(term)) {
            return TermUtils.asJavaInt(term)
                .orElseThrow(() -> new InvalidAstShapeException("integer term", term));
        } else {
            throw new InvalidAstShapeException("integer or Int/UInt application", term);
        }
    }

    private static Integer parseStringToInt(String intString, IStrategoTerm term) {
        try {
            return Integer.parseInt(intString);
        } catch(NumberFormatException e) {
            throw new InvalidAstShapeException("string term representing an integer", term, e);
        }
    }

    public static String toJavaString(IStrategoTerm term) {
        if(TermUtils.isAppl(term, "String", 1)) {
            return tryRemoveDoubleQuotes(TermUtils.toJavaStringAt(term, 0));
        } else if(TermUtils.isAppl(term, "Path", 1) || TermUtils.isAppl(term, "JavaId", 1) || TermUtils.isAppl(term, "SortId", 1) || TermUtils.isAppl(term, "StrategyId", 1)) {
            return TermUtils.toJavaStringAt(term, 0);
        } else if(TermUtils.isString(term)) {
            return tryRemoveDoubleQuotes(TermUtils.toJavaString(term));
        } else {
            throw new InvalidAstShapeException("string or String/Path/JavaId/SortId/StrategyId application", term);
        }
    }

    public static String tryRemoveDoubleQuotes(String string) {
        if(string.startsWith("\"")) {
            string = string.substring(1);
        }
        if(string.endsWith("\"")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    public static char toJavaChar(IStrategoTerm term) {
        if(TermUtils.isAppl(term, "Char", 1)) {
            return tryRemoveSingleQuotes(TermUtils.toJavaStringAt(term, 0)).charAt(0);
        } else {
            throw new InvalidAstShapeException("Char application", term);
        }
    }

    public static String tryRemoveSingleQuotes(String string) {
        if(string.startsWith("'")) {
            string = string.substring(1);
        }
        if(string.endsWith("'")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }
}
