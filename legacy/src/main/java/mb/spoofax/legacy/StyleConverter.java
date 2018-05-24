package mb.spoofax.legacy;

import java.util.ArrayList;

import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.spoofax.api.parse.Token;
import mb.spoofax.api.parse.TokenConstants;
import mb.spoofax.api.parse.TokenImpl;
import mb.spoofax.api.region.Region;
import mb.spoofax.api.style.Style;
import mb.spoofax.api.style.StyleImpl;
import mb.spoofax.api.style.Styling;
import mb.spoofax.api.style.StylingImpl;
import mb.spoofax.api.style.TokenStyle;
import mb.spoofax.api.style.TokenStyleImpl;

public class StyleConverter {
    public static Styling toStyling(Iterable<IRegionStyle<IStrategoTerm>> regionStyles) {
        final ArrayList<TokenStyle> tokenStyles = new ArrayList<>();
        for(IRegionStyle<IStrategoTerm> regionStyle : regionStyles) {
            final Region region = RegionConverter.toRegion(regionStyle.region());
            final Token token = new TokenImpl(region, TokenConstants.unknownType, regionStyle.fragment());
            final Style style = toStyle(regionStyle.style());
            final TokenStyle tokenStyle = new TokenStyleImpl(token, style);
            tokenStyles.add(tokenStyle);
        }
        return new StylingImpl(tokenStyles);
    }


    private static Style toStyle(IStyle style) {
        return new StyleImpl(style.color(), style.backgroundColor(), style.bold(), style.italic(), style.underscore(),
            style.strikeout());
    }
}
