package mb.spoofax2.common.primitive;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax2.common.primitive.generic.ASpoofaxContextPrimitive;
import mb.spoofax2.common.primitive.generic.Spoofax2Context;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AResourcesPrimitive extends ASpoofaxContextPrimitive implements AutoCloseable {
    private static class CacheEntry {
        public final Instant timestamp;
        public final IStrategoTerm term;

        private CacheEntry(Instant timestamp, IStrategoTerm term) {
            this.timestamp = timestamp;
            this.term = term;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final CacheEntry that = (CacheEntry)o;
            return timestamp == that.timestamp &&
                term.equals(that.term);
        }

        @Override public int hashCode() {
            return Objects.hash(timestamp, term);
        }

        @Override public String toString() {
            return "CacheEntry{" +
                "timestamp=" + timestamp +
                ", ast=" + term +
                '}';
        }
    }

    protected final Logger log;
    protected final ResourceService resourceService;
    private final Cache<ResourcePath, CacheEntry> fileCache;

    public AResourcesPrimitive(String name, LoggerFactory loggerFactory, ResourceService resourceService) {
        super(name, 2, 0);
        this.log = loggerFactory.create(AResourcesPrimitive.class);
        this.resourceService = resourceService;
        this.fileCache = CacheBuilder.newBuilder().maximumSize(32).build();
    }

    @Override public void close() {
        fileCache.invalidateAll();
        fileCache.cleanUp();
    }

    @Override protected IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext strategoContext,
        Spoofax2Context context
    ) throws InterpreterException {
        final Strategy nameToPathStr = svars[0];
        final Strategy importStr = svars[1];
        final TermReader termReader = new TermReader(termFactory);

        final Deque<IStrategoTerm> names = Lists.newLinkedList(parseNames(current));
        final Map<IStrategoTerm, IStrategoTerm> resources = Maps.newHashMap();
        while(!names.isEmpty()) {
            final IStrategoTerm name = names.pop();
            if(!resources.containsKey(name)) {
                final String path = resourcePath(strategoContext, nameToPathStr, name);
                final @Nullable IStrategoTerm resource = loadResource(locations(context), path, termReader).orElse(null);
                if(resource == null) {
                    return null;
                }
                resources.put(name, resource);
                names.addAll(resourceImports(strategoContext, importStr, resource));
            }
        }

        return termFactory.makeList(resources.entrySet().stream()
            .map(e -> termFactory.makeTuple(e.getKey(), e.getValue()))
            .collect(Collectors.toList()));
    }

    protected abstract List<HierarchicalResource> locations(Spoofax2Context context);

    private Optional<IStrategoTerm> loadResource(List<HierarchicalResource> locations, String path, TermReader termReader) {
        for(HierarchicalResource location : locations) {
            final HierarchicalResource file;
            try {
                file = resourceService.appendOrReplaceWithHierarchical(location, path);
            } catch(ResourceRuntimeException e) {
                log.warn("Loading resource '{}' from location '{}' failed unexpectedly; skipping location", e, path, location);
                continue;
            }
            final IStrategoTerm term;
            final @Nullable CacheEntry cacheEntry = fileCache.getIfPresent(file.getPath());
            try {
                if(cacheEntry != null && !(cacheEntry.timestamp.isBefore(file.getLastModifiedTime()))) {
                    term = cacheEntry.term;
                } else {
                    try(final InputStream inputStream = file.openRead()) {
                        term = termReader.parseFromStream(inputStream);
                        fileCache.put(file.getPath(), new CacheEntry(file.getLastModifiedTime(), term));
                    }
                }
            } catch(IOException e) {
                log.error("Reading file '{}' failed unexpectedly; skipping file", e, path);
                fileCache.invalidate(file.getPath());
                continue;
            }
            return Optional.of(term);
        }
        log.error("Could not get resource '{}' from locations '{}'", path, locations);
        return Optional.empty();
    }

    private String resourcePath(IContext strategoContext, Strategy s, IStrategoTerm name) throws InterpreterException {
        strategoContext.setCurrent(name);
        if(!s.evaluate(strategoContext)) {
            throw new InterpreterException("Strategy failed to get path for name " + name);
        }
        IStrategoTerm current = strategoContext.current();
        if(!TermUtils.isString(current)) {
            throw new InterpreterException("Expected path string, got " + current);
        }
        return TermUtils.toJavaString(current);

    }

    private List<IStrategoTerm> resourceImports(org.spoofax.interpreter.core.IContext strategoContext, Strategy s,
        IStrategoTerm resource) throws InterpreterException {
        strategoContext.setCurrent(resource);
        if(!s.evaluate(strategoContext)) {
            return Collections.emptyList();
        }
        return parseNames(strategoContext.current());
    }

    private List<IStrategoTerm> parseNames(IStrategoTerm current) throws InterpreterException {
        if(!TermUtils.isList(current)) {
            throw new InterpreterException("Expected list of names, got " + current);
        }
        return Lists.newArrayList(current.getAllSubterms());
    }
}
