package mb.spoofax.compiler.util;

import com.samskivert.mustache.Template;
import mb.pie.api.ExecContext;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

public class TemplateWriter {
    private final Template template;
    private final Charset charset;

    public TemplateWriter(Template template, Charset charset) {
        this.template = template;
        this.charset = charset;
    }

    public HierarchicalResource write(ExecContext execContext, ResourcePath path, Object context) throws IOException {
        final HierarchicalResource resource = execContext.getHierarchicalResource(path);
        resource.createParents();
        try(final Writer writer = writer(resource)) {
            template.execute(context, writer);
            writer.flush();
        }
        execContext.provide(resource);
        return resource;
    }

    public HierarchicalResource write(ExecContext execContext, ResourcePath path, Object context, Object parentContext) throws IOException {
        final HierarchicalResource resource = execContext.getHierarchicalResource(path);
        resource.createParents();
        try(final Writer writer = writer(resource)) {
            template.execute(context, parentContext, writer);
            writer.flush();
        }
        execContext.provide(resource);
        return resource;
    }

    private BufferedWriter writer(WritableResource resource) throws IOException {
        return new BufferedWriter(new ResourceWriter(resource, charset));
    }
}
