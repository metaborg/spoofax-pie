package mb.spoofax.eclipse.transform;

import mb.common.util.ListView;
import mb.spoofax.core.language.transform.TransformInput;
import mb.spoofax.core.language.transform.TransformSubject;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransformUtil {
    public static ListView<TransformInput> input(TransformSubject subject) {
        return ListView.of(new TransformInput(subject));
    }

    public static ListView<TransformInput> inputs(Stream<TransformSubject> subjects) {
        return new ListView<>(subjects.map(TransformInput::new).collect(Collectors.toList()));
    }
}
