package mb.str.incr;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.fs.FSResource;
import mb.str.StrategoScope;
import mb.stratego.build.util.IOAgentTracker;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.common.StrategoIOAgent;
import org.apache.commons.io.output.TeeOutputStream;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

@StrategoScope
public class StrategoIOAgentTrackerFactory implements IOAgentTrackerFactory {
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;

    @Inject public StrategoIOAgentTrackerFactory(LoggerFactory loggerFactory, ResourceService resourceService) {
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
    }

    @Override public IOAgentTracker create(File initialDir, String... excludePatterns) {
        return new StrategoIOAgentTracker(new StrategoIOAgent(loggerFactory, resourceService, new FSResource(initialDir), StrategoIOAgent.defaultStdout(loggerFactory, excludePatterns),
            StrategoIOAgent.defaultStderr(loggerFactory, excludePatterns)), null, null);
    }

    @Override public IOAgentTracker create(File initialDir, OutputStream stdoutStream, OutputStream stderrStream) {
        final ByteArrayOutputStream stdoutLog = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderrLog = new ByteArrayOutputStream();
        final TeeOutputStream stdout = new TeeOutputStream(stdoutStream, stdoutLog);
        final TeeOutputStream stderr = new TeeOutputStream(stderrStream, stderrLog);
        return new StrategoIOAgentTracker(new StrategoIOAgent(loggerFactory, resourceService, new FSResource(initialDir), stdout, stderr), stdoutLog, stderrLog);
    }
}
