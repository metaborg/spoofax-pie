package mb.spoofax.runtime.model.parse;

public class TokenConstants {
    public static final TokenType identifierType = new IdentifierTokenKind();
    public static final TokenType stringType = new StringTokenKind();
    public static final TokenType numberType = new NumberTokenKind();
    public static final TokenType keywordType = new KeywordTokenKind();
    public static final TokenType operatorType = new OperatorTokenKind();
    public static final TokenType layoutType = new LayoutTokenKind();
    public static final TokenType unknownType = new UnknownTokenKind();
}
