package mb.esv.common;

import mb.common.style.Color;
import mb.common.style.Style;
import mb.common.style.StyleImpl;
import mb.common.token.TokenConstants;
import mb.common.token.TokenType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;
import java.util.Map;

class StylingRulesFromESV {
    static ESVStylingRules create(IStrategoTerm esvTerm) {
        final ESVStylingRules styler = new ESVStylingRules();

        final Iterable<IStrategoAppl> styleDefs = ESVReader.collectTerms(esvTerm, "ColorDef");
        final Map<String, Style> namedStyles = new HashMap<>();
        for(IStrategoAppl styleDef : styleDefs) {
            final IStrategoAppl styleTerm = (IStrategoAppl) styleDef.getSubterm(1);
            final IStrategoConstructor styleCons = styleTerm.getConstructor();
            final Style style;
            if(styleCons.getName().equals("Attribute")) {
                style = style(styleTerm);
            } else if(styleCons.getName().equals("AttributeRef")) {
                final String name = asJavaString(styleTerm.getSubterm(0));
                style = namedStyles.get(name);
                if(style == null) {
                    //logger.error("Cannot resolve style definition " + name + " in style definition " + styleDef);
                    // TODO: error tracing
                    continue;
                }
            } else {
                //logger.error("Unhandled style " + styleCons + " in style definition " + styleDef);
                // TODO: error tracing
                continue;
            }

            namedStyles.put(asJavaString(styleDef.getSubterm(0)), style);
        }

        final Iterable<IStrategoAppl> styleRules = ESVReader.collectTerms(esvTerm, "ColorRule");
        for(IStrategoAppl styleRule : styleRules) {
            final IStrategoAppl styleTerm = (IStrategoAppl) styleRule.getSubterm(1);
            final IStrategoConstructor styleCons = styleTerm.getConstructor();
            final Style style;
            if(styleCons.getName().equals("Attribute")) {
                style = style(styleTerm);
            } else if(styleCons.getName().equals("AttributeRef")) {
                final String name = asJavaString(styleTerm.getSubterm(0));
                style = namedStyles.get(name);
                if(style == null) {
                    //logger.error("Cannot resolve style definition " + name + " in style rule " + styleRule);
                    // TODO: error tracing
                    continue;
                }
            } else {
                //logger.error("Unhandled style " + styleCons + " in style rule " + styleRule);
                // TODO: error tracing
                continue;
            }

            final IStrategoAppl node = (IStrategoAppl) styleRule.getSubterm(0);
            final IStrategoConstructor nodeCons = node.getConstructor();
            if(nodeCons.getName().equals("SortAndConstructor")) {
                final String sort = asJavaString(node.getSubterm(0).getSubterm(0));
                final String cons = asJavaString(node.getSubterm(1).getSubterm(0));
                styler.mapSortConsToStyle(sort, cons, style);
            } else if(nodeCons.getName().equals("ConstructorOnly")) {
                final String cons = asJavaString(node.getSubterm(0).getSubterm(0));
                styler.mapConsToStyle(cons, style);
            } else if(nodeCons.getName().equals("Sort")) {
                final String sort = asJavaString(node.getSubterm(0));
                styler.mapSortToStyle(sort, style);
            } else if(nodeCons.getName().equals("Token")) {
                final IStrategoAppl tokenAppl = (IStrategoAppl) node.getSubterm(0);
                final String tokenTypeStr = tokenAppl.getConstructor().getName();
                final TokenType tokenType = tokenType(tokenTypeStr);
                styler.mapTokenTypeToStyle(tokenType, style);
            } else {
                //logger.error("Unhandled node " + nodeCons + " in style rule " + styleRule);
                // TODO: error tracing
                continue;
            }
        }

        return styler;
    }

    private static Style style(IStrategoAppl attribute) {
        final @Nullable Color color = color((IStrategoAppl) attribute.getSubterm(0));
        final @Nullable Color backgroundColor = color((IStrategoAppl) attribute.getSubterm(1));
        final boolean bold;
        final boolean italic;
        final boolean underline = false;
        final boolean strikeout = false;
        final IStrategoAppl fontSetting = (IStrategoAppl) attribute.getSubterm(2);
        final String fontSettingCons = fontSetting.getConstructor().getName();
        switch(fontSettingCons) {
            case "BOLD":
                bold = true;
                italic = false;
                break;
            case "ITALIC":
                bold = false;
                italic = true;
                break;
            case "BOLD_ITALIC":
                bold = true;
                italic = true;
                break;
            default:
                bold = false;
                italic = false;
                break;
        }
        return new StyleImpl(color, backgroundColor, bold, italic, underline, strikeout);
    }

    private static @Nullable Color color(IStrategoAppl color) {
        final String colorCons = color.getConstructor().getName();
        switch(colorCons) {
            case "ColorRGB":
                final int r = Integer.parseInt(asJavaString(color.getSubterm(0)));
                final int g = Integer.parseInt(asJavaString(color.getSubterm(1)));
                final int b = Integer.parseInt(asJavaString(color.getSubterm(2)));
                return new Color(r, g, b);
            case "ColorDefault":
                return Color.black;
            default:
                return null;
        }
    }

    private static TokenType tokenType(String str) {
        switch(str) {
            case "TK_IDENTIFIER":
                return TokenConstants.identifierType;
            case "TK_STRING":
                return TokenConstants.stringType;
            case "TK_NUMBER":
                return TokenConstants.numberType;
            case "TK_KEYWORD":
                return TokenConstants.keywordType;
            case "TK_OPERATOR":
                return TokenConstants.operatorType;
            case "TK_LAYOUT":
                return TokenConstants.layoutType;
            default:
                return TokenConstants.unknownType;
        }
    }

    private static String asJavaString(IStrategoTerm term) {
        return ((IStrategoString) term).stringValue();
    }
}
