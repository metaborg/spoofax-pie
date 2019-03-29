package mb.tiger.spoofax.pie;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.spoofax.core.platform.ResourceService;
import mb.tiger.TigerParser;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ParseTaskDef implements TaskDef<FSPath, @Nullable JSGLR1ParseOutput> {
    private final ResourceService resourceService;
    private final TigerParser parser;

    @Inject public ParseTaskDef(ResourceService resourceService, TigerParser parser) {
        this.resourceService = resourceService;
        this.parser = parser;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable JSGLR1ParseOutput exec(ExecContext context, FSPath path) throws IOException, InterruptedException {
        context.require(path);
        final FSNode node = resourceService.getNode(path);
        if(!node.isFile()) {
            return null;
        }
        final String text = new String(node.readAllBytes(), StandardCharsets.UTF_8);
        return parser.parse(text, "Module");
    }
}
