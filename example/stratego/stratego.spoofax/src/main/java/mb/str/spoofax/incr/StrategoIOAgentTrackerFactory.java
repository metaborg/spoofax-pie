package mb.str.spoofax.incr;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.fs.FSResource;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.build.util.IOAgentTracker;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.common.StrategoIOAgent;

import javax.inject.Inject;
import java.io.File;
import java.io.OutputStream;

@LanguageScope
public class StrategoIOAgentTrackerFactory implements IOAgentTrackerFactory {
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;

    @Inject public StrategoIOAgentTrackerFactory(LoggerFactory loggerFactory, ResourceService resourceService) {
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
    }

    @Override public IOAgentTracker create(File initialDir, String... excludePatterns) {
        // TODO: handle excludePatterns
        return new StrategoIOAgentTracker(new StrategoIOAgent(loggerFactory, resourceService, new FSResource(initialDir)));
    }

    @Override public IOAgentTracker create(File initialDir, OutputStream stdoutStream, OutputStream stderrStream) {
        return new StrategoIOAgentTracker(new StrategoIOAgent(loggerFactory, resourceService, new FSResource(initialDir), stdoutStream, stderrStream));
    }
}
