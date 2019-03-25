package mb.common.token;

public class IdentifierTokenKind implements TokenType {
    @Override public void accept(TokenKindVisitor visitor, Token token) {
        visitor.identifier(token);
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        return getClass() == obj.getClass();
    }

    @Override public String toString() {
        return "identifier";
    }
}
