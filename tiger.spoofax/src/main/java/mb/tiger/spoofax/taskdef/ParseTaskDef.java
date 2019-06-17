package mb.tiger.spoofax.taskdef;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
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
    private final Logger logger;
    private final ResourceService resourceService;
    private final TigerParseTable parseTable;

    @Inject
    public ParseTaskDef(LoggerFactory loggerFactory, ResourceService resourceService, TigerParseTable parseTable) {
        this.logger = loggerFactory.create(getClass());
        this.resourceService = resourceService;
        this.parseTable = parseTable;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public JSGLR1ParseResult exec(ExecContext context, ResourceKey key) throws IOException, InterruptedException {
        final TigerParser parser = new TigerParser(parseTable);
        final ReadableResource resource = resourceService.getResource(key);
        context.require(resource);
        final String text = resource.readString(StandardCharsets.UTF_8);
        logger.trace("Parsing text:\n{}", text);
        return parser.parse(text, "Module");
    }
}
