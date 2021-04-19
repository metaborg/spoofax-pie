package mb.cfg.task;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.util.MultiMap;
import mb.common.util.StreamIterable;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.TypeInfo;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    Optional<ResourcePath> getOneSubtermAsPath(String name, ResourcePath base) {
        return getOneSubtermAsString(name).map(base::appendRelativePath).map(ResourcePath::getNormalized);
    }

    void forOneSubtermAsPath(String name, ResourcePath base, Consumer<ResourcePath> consumer) {
        getOneSubtermAsPath(name, base).ifPresent(consumer);
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

    Stream<ResourcePath> getAllSubTermsAsPaths(String name, ResourcePath base) {
        return getAllSubTermsAsStrings(name).map(base::appendRelativePath).map(ResourcePath::getNormalized);
    }

    void forAllSubtermsAsPaths(String name, ResourcePath base, Consumer<ResourcePath> consumer) {
        getAllSubTermsAsPaths(name, base).forEach(consumer);
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
