package mb.tiger;

import mb.common.region.Region;
import mb.common.style.Color;
import mb.common.style.Styling;
import mb.common.style.TokenStyle;
import mb.common.token.LayoutTokenKind;
import mb.common.token.NumberTokenKind;
import mb.common.token.OperatorTokenKind;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.common.JSGLR1ParseTableException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TigerStylerTest {
    private final TigerParser parser = new TigerParser(TigerParseTable.fromClassLoaderResources());
    private final TigerStyler styler = new TigerStyler(TigerStylingRules.fromClassLoaderResources());

    TigerStylerTest() throws IOException, JSGLR1ParseTableException {}

    @Test void style() throws InterruptedException {
        final JSGLR1ParseResult parseOutput = parser.parse("1 + 21", "Module");
        assertFalse(parseOutput.recovered);
        assertNotNull(parseOutput.ast);
        assertNotNull(parseOutput.tokens);

        final Styling styling = styler.style(parseOutput.tokens);
        final ArrayList<TokenStyle> stylePerToken = styling.stylePerToken();
        assertEquals(5, stylePerToken.size());

        final TokenStyle ts0 = stylePerToken.get(0);
        assertEquals(new NumberTokenKind(), ts0.token().type());
        assertEquals(new Region(0, 0), ts0.token().region());
        final Color numberColor = new Color(255, 0, 128, 0);
        assertEquals(numberColor, ts0.style().color());
        assertNull(ts0.style().backgroundColor());
        assertFalse(ts0.style().bold());
        assertFalse(ts0.style().italic());
        assertFalse(ts0.style().strikeout());
        assertFalse(ts0.style().underscore());

        final TokenStyle ts1 = stylePerToken.get(1);
        assertEquals(new LayoutTokenKind(), ts1.token().type());
        assertEquals(new Region(1, 1), ts1.token().region());
        final Color layoutColor = new Color(255, 63, 127, 95);
        assertEquals(layoutColor, ts1.style().color());
        assertNull(ts1.style().backgroundColor());
        assertFalse(ts1.style().bold());
        assertTrue(ts1.style().italic());
        assertFalse(ts1.style().strikeout());
        assertFalse(ts1.style().underscore());

        final TokenStyle ts2 = stylePerToken.get(2);
        assertEquals(new OperatorTokenKind(), ts2.token().type());
        assertEquals(new Region(2, 2), ts2.token().region());
        final Color operatorColor = new Color(255, 0, 0, 128);
        assertEquals(operatorColor, ts2.style().color());
        assertNull(ts2.style().backgroundColor());
        assertFalse(ts2.style().bold());
        assertFalse(ts2.style().italic());
        assertFalse(ts2.style().strikeout());
        assertFalse(ts2.style().underscore());

        final TokenStyle ts3 = stylePerToken.get(3);
        assertEquals(new LayoutTokenKind(), ts3.token().type());
        assertEquals(new Region(3, 3), ts3.token().region());
        assertEquals(layoutColor, ts3.style().color());
        assertNull(ts3.style().backgroundColor());
        assertFalse(ts3.style().bold());
        assertTrue(ts3.style().italic());
        assertFalse(ts3.style().strikeout());
        assertFalse(ts3.style().underscore());

        final TokenStyle ts4 = stylePerToken.get(4);
        assertEquals(new NumberTokenKind(), ts4.token().type());
        assertEquals(new Region(4, 5), ts4.token().region());
        assertEquals(numberColor, ts4.style().color());
        assertNull(ts4.style().backgroundColor());
        assertFalse(ts4.style().bold());
        assertFalse(ts4.style().italic());
        assertFalse(ts4.style().strikeout());
        assertFalse(ts4.style().underscore());
    }
}