package mb.spoofax.core.language;

import mb.pie.api.Pie;
import mb.pie.api.TaskDefs;
import mb.resource.ResourceService;

public interface LanguageComponent {
    ResourceService getResourceService();

    Pie getPie();

    TaskDefs getTaskDefs();

    LanguageInstance getLanguageInstance();
}
