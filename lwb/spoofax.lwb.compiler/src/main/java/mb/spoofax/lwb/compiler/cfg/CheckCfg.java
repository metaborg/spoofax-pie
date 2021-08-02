package mb.spoofax.lwb.compiler.cfg;

import mb.cfg.task.spoofax.CfgCheck;
import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;

public class CheckCfg implements TaskDef<ResourcePath, KeyedMessages> {
    private final CfgCheck check;

    @Inject public CheckCfg(
        CfgCheck check
    ) {
        this.check = check;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath rootDirectory) {
        return context.require(check, new CfgCheck.Input(rootDirectory.appendRelativePath("spoofaxc.cfg"), rootDirectory));
    }
}
