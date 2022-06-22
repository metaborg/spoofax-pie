package mb.spoofax.lwb.compiler.stratego;

import mb.common.result.Result;
import mb.common.util.ADT;
import mb.common.util.SetView;
import mb.pie.api.OutTransient;
import mb.pie.api.Supplier;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.parsetable.IParseTable;

import java.io.File;
import java.io.Serializable;

@ADT
public abstract class StrategoResolvedDependency implements Serializable {
    interface Cases<R> {
        R sourceDirectory(ResourcePath sourceDirectory);

        R compiledLibrary(ResourcePath str2libFile, SetView<File> javaClassPaths);

        R concreteSyntaxExtensionParseTable(String id, Supplier<? extends Result<? extends IParseTable, ?>> parseTableSupplier);

        R concreteSyntaxExtensionTransientParseTable(String id, Supplier<OutTransient<Result<IParseTable, ?>>> transientParseTableSupplier);
    }

    public static StrategoResolvedDependency sourceDirectory(ResourcePath sourceDirectory) {
        return StrategoResolvedDependencies.sourceDirectory(sourceDirectory);
    }

    public static StrategoResolvedDependency compiledLibrary(ResourcePath str2libFile, SetView<File> javaClassPaths) {
        return StrategoResolvedDependencies.compiledLibrary(str2libFile, javaClassPaths);
    }

    public static StrategoResolvedDependency concreteSyntaxExtensionParseTable(String id, Supplier<? extends Result<? extends IParseTable, ?>> parseTableSupplier) {
        return StrategoResolvedDependencies.concreteSyntaxExtensionParseTable(id, parseTableSupplier);
    }

    public static StrategoResolvedDependency concreteSyntaxExtensionTransientParseTable(String id, Supplier<OutTransient<Result<IParseTable, ?>>> transientParseTableSupplier) {
        return StrategoResolvedDependencies.concreteSyntaxExtensionTransientParseTable(id, transientParseTableSupplier);
    }


    public abstract <R> R match(StrategoResolvedDependency.Cases<R> cases);

    public static StrategoResolvedDependencies.CasesMatchers.TotalMatcher_SourceDirectory cases() {
        return StrategoResolvedDependencies.cases();
    }

    public StrategoResolvedDependencies.CaseOfMatchers.TotalMatcher_SourceDirectory caseOf() {
        return StrategoResolvedDependencies.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
