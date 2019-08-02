package mb.tiger.spoofax.taskdef;

import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.common.JSGLR1ParseResults;
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

public class TigerParse implements TaskDef<ResourceKey, JSGLR1ParseResult> {
    private final ResourceService resourceService;
    private final TigerParseTable parseTable;

    @Inject
    public TigerParse(ResourceService resourceService, TigerParseTable parseTable) {
        this.resourceService = resourceService;
        this.parseTable = parseTable;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public JSGLR1ParseResult exec(ExecContext context, ResourceKey key) throws IOException, InterruptedException {
        final ReadableResource resource = resourceService.getReadableResource(key);
        context.require(resource);
        if(!resource.exists()) {
            return JSGLR1ParseResults.failed(Messages.of(new Message("Cannot parse file '" + key + "', it does not exist", Severity.Error)));
        }
        try {
            final String text = resource.readString(StandardCharsets.UTF_8);
            final TigerParser parser = new TigerParser(parseTable);
            return parser.parse(text, "Module");
        } catch(IOException e) {
            return JSGLR1ParseResults.failed(Messages.of(new Message("Cannot parse file '" + key + "', reading contents failed unexpectedly", e)));
        }
    }
}
