package mb.pipe.run.core.util;

import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.EndNamedGoal;

public class GoalUtil {
    public static CompileGoal makeCompileGoal() {
        return new CompileGoal();
    }

    public static EndNamedGoal makeNamedGoal(String name) {
        return new EndNamedGoal(name);
    }
}
