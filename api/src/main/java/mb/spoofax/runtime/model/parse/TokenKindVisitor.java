package mb.spoofax.runtime.model.parse;

public interface TokenKindVisitor {
    void identifier(Token token);

    void string(Token token);

    void number(Token token);

    void keyword(Token token);

    void operator(Token token);

    void layout(Token token);

    void unknown(Token token);
}
