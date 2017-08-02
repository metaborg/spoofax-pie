package mb.vfs.path;

import java.util.Collection;
import java.util.regex.Pattern;

import mb.vfs.list.AllPathMatcher;
import mb.vfs.list.AllPathWalker;
import mb.vfs.list.DirectoryPathMatcher;
import mb.vfs.list.DirectoryPathWalker;
import mb.vfs.list.ExtensionsPathMatcher;
import mb.vfs.list.PathMatcher;
import mb.vfs.list.PathMatcherWalker;
import mb.vfs.list.PathWalker;
import mb.vfs.list.PatternsPathMatcher;
import mb.vfs.list.RegexPathMatcher;

public class PPaths {
    public static PathMatcher allPathMatcher() {
        return new AllPathMatcher();
    }

    public static PathWalker allPathWalker() {
        return new AllPathWalker();
    }


    public static PathMatcher extensionsPathMatcher(String extension) {
        return new ExtensionsPathMatcher(extension);
    }
    
    public static PathMatcher extensionsPathMatcher(Collection<String> extensions) {
        return new ExtensionsPathMatcher(extensions);
    }

    public static PathWalker extensionsPathWalker(String extension) {
        return new PathMatcherWalker(extensionsPathMatcher(extension));
    }
    
    public static PathWalker extensionsPathWalker(Collection<String> extensions) {
        return new PathMatcherWalker(extensionsPathMatcher(extensions));
    }


    public static PathMatcher patternsPathMatcher(Collection<String> patterns) {
        return new PatternsPathMatcher(patterns);
    }
    
    public static PathMatcher patternsPathMatcher(String pattern) {
        return new PatternsPathMatcher(pattern);
    }

    public static PathWalker patternsPathWalker(Collection<String> patterns) {
        return new PathMatcherWalker(patternsPathMatcher(patterns));
    }
    
    public static PathWalker patternsPathWalker(String pattern) {
        return new PathMatcherWalker(patternsPathMatcher(pattern));
    }


    public static PathMatcher regexPathMatcher(String regex) {
        return new RegexPathMatcher(regex);
    }

    public static PathMatcher regexPathMatcher(Pattern regex) {
        return new RegexPathMatcher(regex);
    }

    public static PathWalker regexPathWalker(String regex) {
        return new PathMatcherWalker(regexPathMatcher(regex));
    }

    public static PathWalker regexPathWalker(Pattern regex) {
        return new PathMatcherWalker(regexPathMatcher(regex));
    }


    public static PathMatcher directoryPathMatcher() {
        // TODO: make ignoring hidden directories configurable
        return new DirectoryPathMatcher(true);
    }

    public static PathWalker directoryPathWalker() {
        // TODO: make ignoring hidden directories configurable
        return new DirectoryPathWalker(true);
    }
}
