package mb.spoofax.compiler.util;

import mb.resource.WritableResource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResourceWriter extends OutputStreamWriter {
    public ResourceWriter(WritableResource resource, Charset charset) throws IOException {
        super(resource.openWrite(), charset);
    }

    public ResourceWriter(WritableResource resource) throws IOException {
        super(resource.openWrite(), StandardCharsets.UTF_8);
    }
}
