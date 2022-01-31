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
        private final Supplier<? extends Result<ParseTable, ?>> completionParseTableSupplier;
        private final ResourcePath atermOutputFile;
        private final ResourcePath persistedOutputFile;
        private final ResourcePath completionAtermOutputFile;
        private final ResourcePath completionPersistedOutputFile;

        public Input(
            Supplier<? extends Result<ParseTable, ?>> parseTableSupplier,
            Supplier<? extends Result<ParseTable, ?>> completionParseTableSupplier,
            ResourcePath atermOutputFile,
            ResourcePath persistedOutputFile,
            ResourcePath completionAtermOutputFile,
            ResourcePath completionPersistedOutputFile
        ) {
            this.parseTableSupplier = parseTableSupplier;
            this.completionParseTableSupplier = completionParseTableSupplier;
            this.atermOutputFile = atermOutputFile;
            this.persistedOutputFile = persistedOutputFile;
            this.completionAtermOutputFile = completionAtermOutputFile;
            this.completionPersistedOutputFile = completionPersistedOutputFile;
        }

        public Key getKey() {
            return new Key(
                atermOutputFile,
                persistedOutputFile,
                completionAtermOutputFile,
                completionPersistedOutputFile
            );
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input that = (Input)o;
            return this.parseTableSupplier.equals(that.parseTableSupplier)
                && this.completionParseTableSupplier.equals(that.completionParseTableSupplier)
                && this.atermOutputFile.equals(that.atermOutputFile)
                && this.persistedOutputFile.equals(that.persistedOutputFile)
                && this.completionAtermOutputFile.equals(that.completionAtermOutputFile)
                && this.completionPersistedOutputFile.equals(that.completionPersistedOutputFile);
        }

        @Override public int hashCode() {
            int result = parseTableSupplier.hashCode();
            result = 31 * result + completionParseTableSupplier.hashCode();
            result = 31 * result + atermOutputFile.hashCode();
            result = 31 * result + persistedOutputFile.hashCode();
            result = 31 * result + completionAtermOutputFile.hashCode();
            result = 31 * result + completionPersistedOutputFile.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Sdf3ParseTableToFile$Input{" +
                "parseTableSupplier=" + parseTableSupplier +
                ", completionParseTableSupplier=" + completionParseTableSupplier +
                ", atermOutputFile=" + atermOutputFile +
                ", persistedOutputFile=" + persistedOutputFile +
                ", completionAtermOutputFile=" + completionAtermOutputFile +
                ", completionPersistedOutputFile=" + completionPersistedOutputFile +
                '}';
        }
    }

    public static class Key implements Serializable {
        private final ResourcePath atermOutputFile;
        private final ResourcePath persistedOutputFile;
        private final ResourcePath completionAtermOutputFile;
        private final ResourcePath completionPersistedOutputFile;

        public Key(ResourcePath atermOutputFile, ResourcePath persistedOutputFile, ResourcePath completionAtermOutputFile, ResourcePath completionPersistedOutputFile) {
            this.atermOutputFile = atermOutputFile;
            this.persistedOutputFile = persistedOutputFile;
            this.completionAtermOutputFile = completionAtermOutputFile;
            this.completionPersistedOutputFile = completionPersistedOutputFile;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Key that = (Key)o;
            return this.atermOutputFile.equals(that.atermOutputFile)
                && this.persistedOutputFile.equals(that.persistedOutputFile)
                && this.completionAtermOutputFile.equals(that.completionAtermOutputFile)
                && this.completionPersistedOutputFile.equals(that.completionPersistedOutputFile);
        }

        @Override public int hashCode() {
            int result = atermOutputFile.hashCode();
            result = 31 * result + persistedOutputFile.hashCode();
            result = 31 * result + completionAtermOutputFile.hashCode();
            result = 31 * result + completionPersistedOutputFile.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Sdf3ParseTableToFile$Key{" +
                "atermOutputFile=" + atermOutputFile +
                ", persistedOutputFile=" + persistedOutputFile +
                ", completionAtermOutputFile=" + completionAtermOutputFile +
                ", completionPersistedOutputFile=" + completionPersistedOutputFile +
                '}';
        }
    }

    @Inject public Sdf3ParseTableToFile() { /* Default @Inject constructor required */ }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<None, ?> exec(ExecContext context, Input input) throws IOException {
        return writeParseTable(
            context,
            input.parseTableSupplier,
            context.getHierarchicalResource(input.atermOutputFile),
            context.getHierarchicalResource(input.persistedOutputFile)
        ).and(writeParseTable(
            context,
            input.completionParseTableSupplier,
            context.getHierarchicalResource(input.completionAtermOutputFile),
            context.getHierarchicalResource(input.completionPersistedOutputFile)
        ));
    }

    @SuppressWarnings("unchecked")
    private <E extends Exception> Result<None, E> writeParseTable(
        ExecContext context,
        Supplier<? extends Result<ParseTable, ?>> parseTableSupplier,
        HierarchicalResource atermOutputFile,
        HierarchicalResource persistedOutputFile
    ) {
        return (Result<None, E>)context.require(parseTableSupplier)
            .mapCatching(parseTable -> {
                atermOutputFile.ensureFileExists();
                atermOutputFile.writeString(ParseTableIO.generateATerm(parseTable).toString(), StandardCharsets.UTF_8);
                context.provide(atermOutputFile);

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
