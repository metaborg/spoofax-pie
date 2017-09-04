package mb.pipe.run.spoofax.util;

import java.util.ArrayList;

import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.model.parse.Token;
import mb.pipe.run.core.model.parse.TokenConstants;
import mb.pipe.run.core.model.parse.TokenImpl;
import mb.pipe.run.core.model.region.Region;
import mb.pipe.run.core.model.style.Style;
import mb.pipe.run.core.model.style.StyleImpl;
import mb.pipe.run.core.model.style.Styling;
import mb.pipe.run.core.model.style.StylingImpl;
import mb.pipe.run.core.model.style.TokenStyle;
import mb.pipe.run.core.model.style.TokenStyleImpl;

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
