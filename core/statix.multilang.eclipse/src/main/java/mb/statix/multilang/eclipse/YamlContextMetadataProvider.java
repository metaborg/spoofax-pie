package mb.statix.multilang.eclipse;

import mb.log.api.Logger;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.utils.ContextUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.util.iterators.Iterables2;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class YamlContextMetadataProvider implements ContextMetadataProvider {

    private static final Logger logger = SpoofaxPlugin.getComponent().getLoggerFactory()
        .create(YamlContextMetadataProvider.class);

    @Override
    public Iterable<Map.Entry<ContextId, ContextConfig>> getContextConfigurations() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        return Stream.of(projects)
            .map(project -> project.getFile("multilang.yaml"))
            .filter(IFile::exists)
            .flatMap(this::readFile)
            ::iterator;
    }

    private Stream<Map.Entry<ContextId, ContextConfig>> readFile(IFile configFile) {
        try {
            MultiLangConfig config = ContextUtils.readYamlConfig(configFile.getContents());
            return config.getCustomContexts().entrySet().stream();
        } catch(CoreException | MultiLangAnalysisException e) {
            logger.warn("Could not load context configurations from " + configFile.getFullPath() + ". Returning empty config");
            return Stream.empty();
        }
    }
}
