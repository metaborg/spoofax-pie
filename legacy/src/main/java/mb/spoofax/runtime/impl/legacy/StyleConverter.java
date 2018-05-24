package mb.spoofax.runtime.impl.legacy;

import java.util.ArrayList;

import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.spoofax.runtime.model.parse.Token;
import mb.spoofax.runtime.model.parse.TokenConstants;
import mb.spoofax.runtime.model.parse.TokenImpl;
import mb.spoofax.runtime.model.region.Region;
import mb.spoofax.runtime.model.style.Style;
import mb.spoofax.runtime.model.style.StyleImpl;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.model.style.StylingImpl;
import mb.spoofax.runtime.model.style.TokenStyle;
import mb.spoofax.runtime.model.style.TokenStyleImpl;

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
