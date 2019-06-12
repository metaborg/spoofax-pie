package mb.tiger.spoofax.taskdef;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.tiger.TigerParseTable;
import mb.tiger.TigerParser;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ParseTaskDef implements TaskDef<ResourceKey, JSGLR1ParseResult> {
    private final ResourceService resourceRegistry;
    private final TigerParseTable parseTable;

    @Inject public ParseTaskDef(ResourceService resourceRegistry, TigerParseTable parseTable) {
        this.resourceRegistry = resourceRegistry;
        this.parseTable = parseTable;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public JSGLR1ParseResult exec(ExecContext context, ResourceKey key) throws IOException, InterruptedException {
        final TigerParser parser = new TigerParser(parseTable);
        final ReadableResource resource = resourceRegistry.getResource(key);
        context.require(resource);
        final String text = resource.readString(StandardCharsets.UTF_8);
        return parser.parse(text, "Module");
    }
}
