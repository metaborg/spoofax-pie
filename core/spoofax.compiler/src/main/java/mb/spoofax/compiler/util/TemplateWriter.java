package mb.spoofax.compiler.util;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;

import java.io.IOException;
import java.nio.charset.Charset;

public class TemplateWriter {
    private final Template template;
    private final ResourceService resourceService;
    private final Charset charset;

    public TemplateWriter(Template template, ResourceService resourceService, Charset charset) {
        this.template = template;
        this.resourceService = resourceService;
        this.charset = charset;
    }

    public HierarchicalResource write(ResourcePath path, Object context) throws IOException {
        final HierarchicalResource resource = resource(path);
        try(final ResourceWriter writer = writer(resource)) {
            template.execute(context, writer);
            writer.flush();
        }
        return resource;
    }

    public HierarchicalResource write(ResourcePath path, Object context, Object parentContext) throws IOException {
        final HierarchicalResource resource = resource(path);
        try(final ResourceWriter writer = writer(resource)) {
            template.execute(context, parentContext, writer);
            writer.flush();
        }
        return resource;
    }

    private HierarchicalResource resource(ResourcePath path) throws IOException {
        return resourceService.getHierarchicalResource(path).createParents();
    }

    private ResourceWriter writer(WritableResource resource) throws IOException {
        return new ResourceWriter(resource, charset);
    }
}
