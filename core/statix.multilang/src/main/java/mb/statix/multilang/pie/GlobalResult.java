package mb.statix.multilang.pie;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.solver.persistent.SolverResult;

import java.io.Serializable;
import java.util.Objects;

public class GlobalResult implements Serializable {
    private final ITerm globalScope;
    private final ITermVar globalScopeVar;
    private final SolverResult result;

    public GlobalResult(ITerm globalScope, ITermVar globalScopeVar, SolverResult result) {
        this.globalScope = globalScope;
        this.globalScopeVar = globalScopeVar;
        this.result = result;
    }

    public ITerm getGlobalScope() {
        return globalScope;
    }

    public ITermVar getGlobalScopeVar() {
        return globalScopeVar;
    }

    public SolverResult getResult() {
        return result;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        GlobalResult output = (GlobalResult)o;
        return globalScope.equals(output.globalScope) &&
            globalScopeVar.equals(output.globalScopeVar) &&
            result.equals(output.result);
    }

    @Override public int hashCode() {
        return Objects.hash(globalScope, globalScopeVar, result);
    }

    @Override public String toString() {
        return "Output{" +
            "globalScope=" + globalScope +
            ", globalScopeVar=" + globalScopeVar +
            ", result=" + result +
            '}';
    }
}
