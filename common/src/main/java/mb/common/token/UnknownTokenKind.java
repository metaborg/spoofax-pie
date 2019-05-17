package mb.common.token;

// DP: Final?
public class UnknownTokenKind implements TokenType {

    // DP: Shouldn't this be a singleton instance?

    @Override public void accept(TokenKindVisitor visitor, Token token) {
        visitor.unknown(token);
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
        return "unknown";
    }
}
