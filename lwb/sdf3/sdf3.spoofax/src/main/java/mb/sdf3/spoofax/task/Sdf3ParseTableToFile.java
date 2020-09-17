package mb.sdf3.spoofax.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@LanguageScope
public class Sdf3ParseTableToFile implements TaskDef<Sdf3ParseTableToFile.Args, Result<None, ?>> {
    public static class Args implements Serializable {
        private final Supplier<? extends Result<ParseTable, ?>> parseTableSupplier;
        private final ResourceKey outputFile;

        public Args(Supplier<? extends Result<ParseTable, ?>> parseTableSupplier, ResourceKey outputFile) {
            this.parseTableSupplier = parseTableSupplier;
            this.outputFile = outputFile;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Args args = (Args)o;
            return parseTableSupplier.equals(args.parseTableSupplier) && outputFile.equals(args.outputFile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parseTableSupplier, outputFile);
        }
    }

    @Inject public Sdf3ParseTableToFile() { /* Default @Inject constructor required */ }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<None, ?> exec(ExecContext context, Args args) throws IOException {
        return context.require(args.parseTableSupplier)
            .mapCatching(parseTable -> {
                final IStrategoTerm parseTableTerm = ParseTableIO.generateATerm(parseTable);
                final WritableResource writableResource = context.getWritableResource(args.outputFile);
                writableResource.writeString(parseTable.toString(), StandardCharsets.UTF_8);
                context.provide(writableResource);
                return None.instance;
            });
    }
}
