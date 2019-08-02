package mb.spoofax.eclipse.transform;

import mb.common.util.ListView;
import mb.spoofax.core.language.transform.TransformContext;
import mb.spoofax.core.language.transform.TransformInput;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransformUtil {
    public static ListView<TransformInput> input(TransformContext context) {
        return ListView.of(new TransformInput(context));
    }

    public static ListView<TransformInput> inputs(Stream<TransformContext> contexts) {
        return new ListView<>(contexts.map(TransformInput::new).collect(Collectors.toList()));
    }
}
