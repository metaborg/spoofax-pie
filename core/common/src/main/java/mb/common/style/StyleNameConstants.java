package mb.common.style;

@SuppressWarnings("unused")
public final class StyleNameConstants {
    private StyleNameConstants() {}

    // @formatter:off
    public static final String ENTITY_ANNOTATION    = "meta.annotation";            // [1]
    public static final String ENTITY_BLOCK         = "meta.block";                 // [1]
    public static final String ENTITY_BRACES        = "meta.braces";                // [1]
    public static final String ENTITY_BRACKETS      = "meta.brackets";              // [1]
    public static final String ENTITY_CLASS         = "meta.class";                 // [1]
    public static final String ENTITY_ENUM          = "meta.enum";                  // [1]
    public static final String ENTITY_EXCEPTION     = "meta.exception";
    public static final String ENTITY_EXCLUDED      = "meta.excluded";
    public static final String ENTITY_FIELD         = "meta.field";
    public static final String ENTITY_FUNCTION      = "meta.function";              // [1]
    public static final String ENTITY_FUNCTIONCALL  = "meta.function-call";         // [1]
    public static final String ENTITY_GENERIC       = "meta.generic";               // [1]
    public static final String ENTITY_GROUP         = "meta.group";                 // [1]
    public static final String ENTITY_IMPL          = "meta.impl";                  // [1]
    public static final String ENTITY_INTERFACE     = "meta.interface";             // [1]
    public static final String ENTITY_INTERPOLATION = "meta.interpolation";         // [1]
    public static final String ENTITY_METHOD        = "meta.method";
    public static final String ENTITY_MODULE        = "meta.module";
    public static final String ENTITY_NAMESPACE     = "meta.namespace";             // [1]
    public static final String ENTITY_PACKAGE       = "meta.package";
    public static final String ENTITY_PARAGRAPH     = "meta.paragraph";             // [1]
    public static final String ENTITY_PARENS        = "meta.parens";                // [1]
    public static final String ENTITY_PATH          = "meta.path";                  // [1]
    public static final String ENTITY_PREPROCESSOR  = "meta.preprocessor";          // [1]
    public static final String ENTITY_PROPERTY      = "meta.property";
    public static final String ENTITY_STRING        = "meta.string";                // [1]
    public static final String ENTITY_STRUCT        = "meta.struct";                // [1]
    public static final String ENTITY_TAG           = "meta.tag";                   // [1]
    public static final String ENTITY_TEST          = "meta.test";
    public static final String ENTITY_TRAIT         = "meta.trait";                 // [1]
    public static final String ENTITY_TYPE          = "meta.type";                  // [1]
    public static final String ENTITY_VARIABLE      = "meta.variable";
    public static final String ENTITY_UNION         = "meta.union";                 // [1]
    // @formatter:on

    // @formatter:off
    public static final String VISIBILITY_PRIVATE   = "storage.modifier.private";   // [1]
    public static final String VISIBILITY_PUBLIC    = "storage.modifier.public";    // [1]
    public static final String VISIBILITY_PACKAGE   = "storage.modifier.package";
    public static final String VISIBILITY_INTERNAL  = "storage.modifier.internal";
    public static final String VISIBILITY_PROTECTED = "storage.modifier.protected";
    // @formatter:on

    // @formatter:off
    public static final String STORAGE_STATIC       = "storage.modifier.static";    // [1]
    public static final String STORAGE_EXTERNAL     = "storage.modifier.external";
    public static final String STORAGE_INLINE       = "storage.modifier.inline";    // [1]
    public static final String STORAGE_CONST        = "storage.modifier.const";     // [1]
    // @formatter:on

    // @formatter:off
    public static final String COMMENT_LINE         = "comment.line";                   // [2]
    public static final String COMMENT_BLOCK        = "comment.block";                  // [2]
    public static final String COMMENT_DOC          = "comment.block.documentation";    // [2]
    // @formatter:on

    // @formatter:off
    public static final String CONSTANT_NUMERIC             = "constant.numeric";               // [2]
    public static final String CONSTANT_CHARACTER           = "constant.character";             // [2]
    public static final String CONSTANT_CHARACTER_ESCAPE    = "constant.character.escape";      // [2]
    public static final String CONSTANT_LANGUAGE            = "constant.language";              // [2]
    public static final String CONSTANT                     = "constant";                       // [2]
    // @formatter:on

    // [1]: https://www.sublimetext.com/docs/3/scope_naming.html
    // [2]: https://macromates.com/manual/en/language_grammars#naming_conventions
}
