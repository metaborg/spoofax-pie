package mb.spoofax.compiler.util;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
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

    public void write(Object context, ResourcePath path) throws IOException {
        try(final ResourceWriter writer = writer(path)) {
            template.execute(context, writer);
            writer.flush();
        }
    }

    public void write(Object context, Object parentContext, ResourcePath path) throws IOException {
        try(final ResourceWriter writer = writer(path)) {
            template.execute(context, parentContext, writer);
            writer.flush();
        }
    }

    private ResourceWriter writer(ResourcePath path) throws IOException {
        return new ResourceWriter(resourceService.getHierarchicalResource(path).createParents(), charset);
    }
}
