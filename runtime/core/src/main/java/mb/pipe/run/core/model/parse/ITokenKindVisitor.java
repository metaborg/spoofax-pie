package mb.pipe.run.core.model.parse;

public interface ITokenKindVisitor {
    void identifier(IToken token);

    void string(IToken token);

    void number(IToken token);

    void keyword(IToken token);

    void operator(IToken token);

    void layout(IToken token);

    void unknown(IToken token);
}
