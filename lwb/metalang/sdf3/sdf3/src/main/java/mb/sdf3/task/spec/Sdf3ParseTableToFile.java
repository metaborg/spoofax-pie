package mb.sdf3.task.spec;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3Scope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.io.ParseTableIO;
import org.metaborg.sdf2table.parsetable.ParseTable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Sdf3Scope
public class Sdf3ParseTableToFile implements TaskDef<Sdf3ParseTableToFile.Args, Result<None, ?>> {
    public static class Args implements Serializable {
        private final Supplier<? extends Result<ParseTable, ?>> parseTableSupplier;
        private final ResourcePath outputFile;

        public Args(Supplier<? extends Result<ParseTable, ?>> parseTableSupplier, ResourcePath outputFile) {
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
                final HierarchicalResource resource = context.getHierarchicalResource(args.outputFile);
                resource.ensureFileExists();
                resource.writeString(ParseTableIO.generateATerm(parseTable).toString(), StandardCharsets.UTF_8);
                context.provide(resource);
                return None.instance;
            });
    }
}
