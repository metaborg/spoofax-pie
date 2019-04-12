package mb.spoofax.core.language;

import mb.pie.api.TaskDef;

import javax.inject.Named;
import java.util.Set;

@LanguageScope
public interface LanguageComponent {
    LanguageInstance getLanguageInstance();

    @Named("language") Set<TaskDef<?, ?>> getTaskDefs();
}
