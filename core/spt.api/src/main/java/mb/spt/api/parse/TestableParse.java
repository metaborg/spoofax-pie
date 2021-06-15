package mb.spt.api.parse;

import mb.common.result.Result;
import mb.pie.api.Session;
import mb.spt.api.model.TestCase;

public interface TestableParse {
    Result<ParseResult, ?> testParse(Session session, TestCase testCase) throws InterruptedException;
}
