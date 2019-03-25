package mb.common.token;

public class LayoutTokenKind implements TokenType {
    @Override public void accept(TokenKindVisitor visitor, Token token) {
        visitor.layout(token);
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
        return "layout";
    }
}
