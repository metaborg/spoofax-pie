package mb.sdf3.task.spec;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Sdf3Scope
public class Sdf3ParseTableToFile implements TaskDef<Sdf3ParseTableToFile.Input, Result<None, ?>> {
    public static class Input implements Serializable {
        private final Supplier<? extends Result<ParseTable, ?>> parseTableSupplier;
        private final ResourcePath atermOutputFile;
        private final ResourcePath persistedOutputFile;

        public Input(
            Supplier<? extends Result<ParseTable, ?>> parseTableSupplier,
            ResourcePath atermOutputFile,
            ResourcePath persistedOutputFile
        ) {
            this.parseTableSupplier = parseTableSupplier;
            this.atermOutputFile = atermOutputFile;
            this.persistedOutputFile = persistedOutputFile;
        }

        public Key getKey() {
            return new Key(atermOutputFile, persistedOutputFile);
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!parseTableSupplier.equals(input.parseTableSupplier)) return false;
            if(!atermOutputFile.equals(input.atermOutputFile)) return false;
            return persistedOutputFile.equals(input.persistedOutputFile);
        }

        @Override public int hashCode() {
            int result = parseTableSupplier.hashCode();
            result = 31 * result + atermOutputFile.hashCode();
            result = 31 * result + persistedOutputFile.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Sdf3ParseTableToFile$Input{" +
                "parseTableSupplier=" + parseTableSupplier +
                ", atermOutputFile=" + atermOutputFile +
                ", persistedOutputFile=" + persistedOutputFile +
                '}';
        }
    }

    public static class Key implements Serializable {
        private final ResourcePath atermOutputFile;
        private final ResourcePath persistedOutputFile;

        public Key(ResourcePath atermOutputFile, ResourcePath persistedOutputFile) {
            this.atermOutputFile = atermOutputFile;
            this.persistedOutputFile = persistedOutputFile;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Key key = (Key)o;
            if(!atermOutputFile.equals(key.atermOutputFile)) return false;
            return persistedOutputFile.equals(key.persistedOutputFile);
        }

        @Override public int hashCode() {
            int result = atermOutputFile.hashCode();
            result = 31 * result + persistedOutputFile.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Sdf3ParseTableToFile$Key{" +
                "atermOutputFile=" + atermOutputFile +
                ", persistedOutputFile=" + persistedOutputFile +
                '}';
        }
    }

    @Inject public Sdf3ParseTableToFile() { /* Default @Inject constructor required */ }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<None, ?> exec(ExecContext context, Input input) throws IOException {
        return context.require(input.parseTableSupplier)
            .mapCatching(parseTable -> {
                final HierarchicalResource atermOutputFile = context.getHierarchicalResource(input.atermOutputFile);
                atermOutputFile.ensureFileExists();
                atermOutputFile.writeString(ParseTableIO.generateATerm(parseTable).toString(), StandardCharsets.UTF_8);
                context.provide(atermOutputFile);

                final HierarchicalResource persistedOutputFile = context.getHierarchicalResource(input.persistedOutputFile);
                persistedOutputFile.ensureFileExists();
                try(final ObjectOutputStream stream = new ObjectOutputStream(persistedOutputFile.openWrite())) {
                    stream.writeObject(parseTable);
                    stream.flush();
                }
                return None.instance;
            });
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public Serializable key(Input input) {
        return input.getKey();
    }
}
