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
import mb.log.noop.NoopLoggerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TigerStylerTest {
    private final TigerParser parser = new TigerParser(TigerParseTable.fromClassLoaderResources());
    private final TigerStyler styler =
        new TigerStyler(TigerStylingRules.fromClassLoaderResources(), new NoopLoggerFactory());

    TigerStylerTest() throws IOException, JSGLR1ParseTableException {}

    @Test void style() throws InterruptedException {
        final JSGLR1ParseResult parseOutput = parser.parse("1 + 21", "Module");
        assertFalse(parseOutput.recovered);
        assertNotNull(parseOutput.ast);
        assertNotNull(parseOutput.tokens);

        final Styling styling = styler.style(parseOutput.tokens);
        final ArrayList<TokenStyle> stylePerToken = styling.getStylePerToken();
        assertEquals(5, stylePerToken.size());

        final TokenStyle ts0 = stylePerToken.get(0);
        assertEquals(new NumberTokenKind(), ts0.getToken().getType());
        assertEquals(Region.fromOffsets(0, 1), ts0.getToken().getRegion());
        final Color numberColor = new Color(255, 0, 128, 0);
        assertEquals(numberColor, ts0.getStyle().getColor());
        assertNull(ts0.getStyle().getBackgroundColor());
        assertFalse(ts0.getStyle().getIsBold());
        assertFalse(ts0.getStyle().getIsItalic());
        assertFalse(ts0.getStyle().getIsStrikeout());
        assertFalse(ts0.getStyle().getIsUnderscore());

        final TokenStyle ts1 = stylePerToken.get(1);
        assertEquals(new LayoutTokenKind(), ts1.getToken().getType());
        assertEquals(Region.fromOffsets(1, 2), ts1.getToken().getRegion());
        final Color layoutColor = new Color(255, 63, 127, 95);
        assertEquals(layoutColor, ts1.getStyle().getColor());
        assertNull(ts1.getStyle().getBackgroundColor());
        assertFalse(ts1.getStyle().getIsBold());
        assertTrue(ts1.getStyle().getIsItalic());
        assertFalse(ts1.getStyle().getIsStrikeout());
        assertFalse(ts1.getStyle().getIsUnderscore());

        final TokenStyle ts2 = stylePerToken.get(2);
        assertEquals(new OperatorTokenKind(), ts2.getToken().getType());
        assertEquals(Region.fromOffsets(2, 3), ts2.getToken().getRegion());
        final Color operatorColor = new Color(255, 0, 0, 128);
        assertEquals(operatorColor, ts2.getStyle().getColor());
        assertNull(ts2.getStyle().getBackgroundColor());
        assertFalse(ts2.getStyle().getIsBold());
        assertFalse(ts2.getStyle().getIsItalic());
        assertFalse(ts2.getStyle().getIsStrikeout());
        assertFalse(ts2.getStyle().getIsUnderscore());

        final TokenStyle ts3 = stylePerToken.get(3);
        assertEquals(new LayoutTokenKind(), ts3.getToken().getType());
        assertEquals(Region.fromOffsets(3, 4), ts3.getToken().getRegion());
        assertEquals(layoutColor, ts3.getStyle().getColor());
        assertNull(ts3.getStyle().getBackgroundColor());
        assertFalse(ts3.getStyle().getIsBold());
        assertTrue(ts3.getStyle().getIsItalic());
        assertFalse(ts3.getStyle().getIsStrikeout());
        assertFalse(ts3.getStyle().getIsUnderscore());

        final TokenStyle ts4 = stylePerToken.get(4);
        assertEquals(new NumberTokenKind(), ts4.getToken().getType());
        assertEquals(Region.fromOffsets(4, 6), ts4.getToken().getRegion());
        assertEquals(numberColor, ts4.getStyle().getColor());
        assertNull(ts4.getStyle().getBackgroundColor());
        assertFalse(ts4.getStyle().getIsBold());
        assertFalse(ts4.getStyle().getIsItalic());
        assertFalse(ts4.getStyle().getIsStrikeout());
        assertFalse(ts4.getStyle().getIsUnderscore());
    }
}