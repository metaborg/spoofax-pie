package mb.spoofax.legacy;

import mb.spoofax.api.parse.*;
import mb.spoofax.api.region.Region;
import mb.spoofax.api.style.*;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.util.ArrayList;

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
        return new StyleImpl(toColor(style.color()), toColor(style.backgroundColor()), style.bold(), style.italic(), style.underscore(),
            style.strikeout());
    }

    private static Color toColor(@Nullable java.awt.Color color) {
        if(color == null) {
            return null;
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
}
