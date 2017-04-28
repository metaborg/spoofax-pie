package mb.pipe.run.core.model.parse;

public class TokenConstants {
    public static final ITokenType identifierType = new IdentifierTokenKind();
    public static final ITokenType stringType = new StringTokenKind();
    public static final ITokenType numberType = new NumberTokenKind();
    public static final ITokenType keywordType = new KeywordTokenKind();
    public static final ITokenType operatorType = new OperatorTokenKind();
    public static final ITokenType layoutType = new LayoutTokenKind();
    public static final ITokenType unknownType = new UnknownTokenKind();
}
