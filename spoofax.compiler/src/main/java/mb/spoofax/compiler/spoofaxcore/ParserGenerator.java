package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.spoofax.compiler.util.MustacheUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class ParserGenerator {
    private final Template parseTableTemplate;

    private ParserGenerator(Template parseTableTemplate) {
        this.parseTableTemplate = parseTableTemplate;
    }

    public static ParserGenerator fromClassLoaderResources() {
        final Class<?> clazz = ParserGenerator.class;
        return new ParserGenerator(MustacheUtil.compile(clazz, "ParseTable.java.mustache"));
    }


    public void generate(ParserInput input) throws IOException {
        try(final Writer writer = new PrintWriter(System.out)) {
            parseTableTemplate.execute(input, input.basicInput(), writer);
            writer.flush();
        }
    }
}
