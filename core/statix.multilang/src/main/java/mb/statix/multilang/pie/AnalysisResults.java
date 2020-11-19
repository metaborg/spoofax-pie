package mb.statix.multilang.pie;

import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.FileResult;
import mb.statix.solver.persistent.SolverResult;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashMap;

@Value.Immutable
public abstract class AnalysisResults implements Serializable {
    @Value.Parameter public abstract HashMap<LanguageId, Result<SolverResult, MultiLangAnalysisException>> projectResults();

    @Value.Parameter public abstract HashMap<FileKey, Result<FileResult, MultiLangAnalysisException>> fileResults();

    @Value.Parameter public abstract Result<SolverResult, MultiLangAnalysisException> finalResult();

    // Custom toString to prevent memory leaks when logging
    @Override public String toString() {
        return "AnalysisResults{" +
            ", projectResults=" + projectResults().keySet() +
            ", fileResults=" + fileResults().keySet() +
            ", finalResult=" + finalResult().getClass().getSimpleName() +
            '}';
    }
}
