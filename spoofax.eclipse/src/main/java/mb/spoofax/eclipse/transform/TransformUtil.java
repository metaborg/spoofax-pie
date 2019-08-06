package mb.spoofax.eclipse.transform;

import mb.common.util.ListView;
import mb.pie.api.Task;
import mb.spoofax.core.language.transform.TransformContext;
import mb.spoofax.core.language.transform.TransformDef;
import mb.spoofax.core.language.transform.TransformInput;
import mb.spoofax.core.language.transform.TransformOutput;
import mb.spoofax.core.language.transform.param.RawArgs;
import mb.spoofax.core.language.transform.param.RawArgsBuilder;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransformUtil {
    public static ListView<TransformContext> context(TransformContext context) {
        return ListView.of(context);
    }

    public static ListView<TransformContext> contexts(Stream<TransformContext> contexts) {
        return new ListView<>(contexts.collect(Collectors.toList()));
    }

    public static <A extends Serializable> Task<TransformOutput> createTask(TransformDef<A> def, TransformContext context) {
        final RawArgsBuilder builder = new RawArgsBuilder(def.getParamDef());
        final RawArgs rawArgs = builder.build(context);
        final A args = def.fromRawArgs(rawArgs);
        final TransformInput<A> input = new TransformInput<>(args);
        return def.createTask(input);
    }
}
