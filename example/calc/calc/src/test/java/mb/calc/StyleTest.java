package mb.calc;

import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.style.Color;
import mb.common.style.Styling;
import mb.common.style.TokenStyle;
import mb.common.token.TokenTypes;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StyleTest extends TestBase {
    @Test void testParseTask() throws Exception {
        final FSResource resource = textFile("test.calc", "1 + 2;");
        try(final MixedSession session = newSession()) {
            final Option<Styling> stylingOption = session.require(component.getCalcStyle().createTask(component.getCalcParse().createTokensSupplier(resource.getKey()).map(Result::ok)));
            assertTrue(stylingOption.isSome());
            final Styling styling = stylingOption.get();
            assertNotNull(styling);

            final ArrayList<TokenStyle> stylePerToken = styling.getStylePerToken();
            assertEquals(6, stylePerToken.size());

            final TokenStyle ts0 = stylePerToken.get(0);
            assertEquals(TokenTypes.identifier(), ts0.getToken().getType());
            assertEquals(Region.fromOffsets(0, 1, 0), ts0.getToken().getRegion());
            final Color identifierColor = new Color(255, 0, 0, 0);
            assertEquals(identifierColor, ts0.getStyle().getColor());
            assertNull(ts0.getStyle().getBackgroundColor());
            assertFalse(ts0.getStyle().isBold());
            assertFalse(ts0.getStyle().isItalic());
            assertFalse(ts0.getStyle().isStrikeout());
            assertFalse(ts0.getStyle().isUnderscore());

            final TokenStyle ts1 = stylePerToken.get(1);
            assertEquals(TokenTypes.layout(), ts1.getToken().getType());
            assertEquals(Region.fromOffsets(1, 2, 0), ts1.getToken().getRegion());
            final Color layoutColor = new Color(255, 63, 127, 95);
            assertEquals(layoutColor, ts1.getStyle().getColor());
            assertNull(ts1.getStyle().getBackgroundColor());
            assertFalse(ts1.getStyle().isBold());
            assertTrue(ts1.getStyle().isItalic());
            assertFalse(ts1.getStyle().isStrikeout());
            assertFalse(ts1.getStyle().isUnderscore());

            final TokenStyle ts2 = stylePerToken.get(2);
            assertEquals(TokenTypes.operator(), ts2.getToken().getType());
            assertEquals(Region.fromOffsets(2, 3, 0), ts2.getToken().getRegion());
            final Color operatorColor = new Color(255, 0, 0, 128);
            assertEquals(operatorColor, ts2.getStyle().getColor());
            assertNull(ts2.getStyle().getBackgroundColor());
            assertFalse(ts2.getStyle().isBold());
            assertFalse(ts2.getStyle().isItalic());
            assertFalse(ts2.getStyle().isStrikeout());
            assertFalse(ts2.getStyle().isUnderscore());

            final TokenStyle ts3 = stylePerToken.get(3);
            assertEquals(TokenTypes.layout(), ts3.getToken().getType());
            assertEquals(Region.fromOffsets(3, 4, 0), ts3.getToken().getRegion());
            assertEquals(layoutColor, ts3.getStyle().getColor());
            assertNull(ts3.getStyle().getBackgroundColor());
            assertFalse(ts3.getStyle().isBold());
            assertTrue(ts3.getStyle().isItalic());
            assertFalse(ts3.getStyle().isStrikeout());
            assertFalse(ts3.getStyle().isUnderscore());

            final TokenStyle ts4 = stylePerToken.get(4);
            assertEquals(TokenTypes.identifier(), ts4.getToken().getType());
            assertEquals(Region.fromOffsets(4, 5, 0), ts4.getToken().getRegion());
            assertEquals(identifierColor, ts4.getStyle().getColor());
            assertNull(ts4.getStyle().getBackgroundColor());
            assertFalse(ts4.getStyle().isBold());
            assertFalse(ts4.getStyle().isItalic());
            assertFalse(ts4.getStyle().isStrikeout());
            assertFalse(ts4.getStyle().isUnderscore());

            final TokenStyle ts5 = stylePerToken.get(5);
            assertEquals(TokenTypes.operator(), ts5.getToken().getType());
            assertEquals(Region.fromOffsets(5, 6, 0), ts5.getToken().getRegion());
            assertEquals(operatorColor, ts5.getStyle().getColor());
            assertNull(ts5.getStyle().getBackgroundColor());
            assertFalse(ts5.getStyle().isBold());
            assertFalse(ts5.getStyle().isItalic());
            assertFalse(ts5.getStyle().isStrikeout());
            assertFalse(ts5.getStyle().isUnderscore());
        }
    }
}
