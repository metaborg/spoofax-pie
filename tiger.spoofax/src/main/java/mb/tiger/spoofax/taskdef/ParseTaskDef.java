package mb.tiger.spoofax.taskdef;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.tiger.TigerParser;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ParseTaskDef implements TaskDef<ResourceKey, JSGLR1ParseResult> {
    private final ResourceService resourceRegistry;
    private final TigerParser parser;

    @Inject public ParseTaskDef(ResourceService resourceRegistry, TigerParser parser) {
        this.resourceRegistry = resourceRegistry;
        this.parser = parser;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public JSGLR1ParseResult exec(ExecContext context, ResourceKey key) throws IOException, InterruptedException {
        final ReadableResource resource = resourceRegistry.getReadableResource(key);
        context.require(resource);
        final String text = resource.readString(StandardCharsets.UTF_8);
        return parser.parse(text, "Module");
    }
}
