package mb.spt.api.parse;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.spt.api.model.TestCase;

public interface TestableParse {
    Result<ParseResult, ?> testParse(ExecContext context, TestCase testCase);
}
