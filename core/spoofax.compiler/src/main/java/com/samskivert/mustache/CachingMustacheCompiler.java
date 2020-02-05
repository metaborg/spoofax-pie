package com.samskivert.mustache;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CachingMustacheCompiler extends Mustache.Compiler {
    private final ConcurrentHashMap<String, Template> templateCache = new ConcurrentHashMap<>();

    public CachingMustacheCompiler(boolean standardsMode, boolean strictSections, String nullValue, boolean missingIsNull, boolean emptyStringIsFalse, boolean zeroIsFalse, Mustache.Formatter formatter, Mustache.Escaper escaper, Mustache.TemplateLoader loader, Mustache.Collector collector, Mustache.Delims delims) {
        super(standardsMode, strictSections, nullValue, missingIsNull, emptyStringIsFalse, zeroIsFalse, formatter, escaper, loader, collector, delims);
    }

    public static CachingMustacheCompiler cachingCompiler() {
        return new CachingMustacheCompiler(false, true, null, false,
            false, false, Mustache.DEFAULT_FORMATTER, Escapers.NONE, Mustache.FAILING_LOADER,
            new DefaultCollector() {
                public Iterator<?> toIterator(final Object value) {
                    if(value instanceof Optional<?>) { // Support Optional values that are not present.
                        final Optional<?> opt = (Optional<?>)value;
                        return opt.isPresent() ? Collections.singleton(opt.get()).iterator() : Collections.emptyIterator();
                    } else return super.toIterator(value);
                }
            }, new Mustache.Delims());
    }

    public CachingMustacheCompiler withLoader(Mustache.TemplateLoader loader) {
        return new CachingMustacheCompiler(this.standardsMode, this.strictSections, this.nullValue, this.missingIsNull,
            this.emptyStringIsFalse, this.zeroIsFalse, this.formatter, this.escaper, loader, this.collector, this.delims);
    }

    @Override public Template loadTemplate(String name) throws MustacheException {
        return templateCache.computeIfAbsent(name, super::loadTemplate);
    }
}
