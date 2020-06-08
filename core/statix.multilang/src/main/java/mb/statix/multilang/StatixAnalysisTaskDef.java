package mb.statix.multilang;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.ITermFactory;

import java.io.Serializable;

public class StatixAnalysisTaskDef implements TaskDef<StatixAnalysisTaskDef.Input, @Nullable KeyedMessages> {

    public static class Input implements Serializable {
        private AnalysisContext context;

        public Input(AnalysisContext context) {
            this.context = context;
        }
    }

    private final ITermFactory tf;

    public StatixAnalysisTaskDef(ITermFactory tf) {
        this.tf = tf;
    }

    @Override
    public String getId() {
        return StatixAnalysisTaskDef.class.getSimpleName();
    }

    @Override
    public @Nullable KeyedMessages exec(ExecContext context, Input input) throws Exception {
        return new SolverContext(input.context, context, tf).execute();
    }
}
