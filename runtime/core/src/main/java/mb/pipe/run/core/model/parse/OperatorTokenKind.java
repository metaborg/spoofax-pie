package mb.pipe.run.core.model.parse;

public class OperatorTokenKind implements TokenType {
    private static final long serialVersionUID = 1L;


    @Override public void accept(TokenKindVisitor visitor, Token token) {
        visitor.operator(token);
    }


    @Override public int hashCode() {
        return 0;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override public String toString() {
        return "operator";
    }
}
