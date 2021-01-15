package mb.spoofax.dynamicloading.task;

import mb.common.option.Option;
import mb.common.style.Styling;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.OutTransient;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;

public class DynamicStyle implements TaskDef<ResourceKey, Option<Styling>> {
    private final ReloadLanguageComponent reloadLanguageComponent;

    public DynamicStyle(
        ReloadLanguageComponent reloadLanguageComponent
    ) {
        this.reloadLanguageComponent = reloadLanguageComponent;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Option<Styling> exec(ExecContext context, ResourceKey input) throws Exception {
        final OutTransient<LanguageComponent> output = context.require(reloadLanguageComponent, None.instance);
        final LanguageInstance languageInstance = output.getValue().getLanguageInstance();
        // TODO: there is no connection between the following styling task and the dynamic reload task, meaning they
        // could happen in any order in a bottom-up traversal. This may cause styling to be calculated before the
        // language is recompiled, which is wrong!
        return context.require(languageInstance.createStyleTask(input));
    }
}
