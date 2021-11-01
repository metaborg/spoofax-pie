package mb.spoofax.lwb.compiler.util;

import mb.common.util.ResourceUtil;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;

import java.io.IOException;

public abstract class TaskCopyUtil {
    public static void copy(ExecContext context, ResourceKey input, ResourceKey output) throws IOException {
        final WritableResource outputFile = context.getWritableResource(output);
        ResourceUtil.copy(context.require(input), outputFile);
        context.provide(outputFile);
    }
}
