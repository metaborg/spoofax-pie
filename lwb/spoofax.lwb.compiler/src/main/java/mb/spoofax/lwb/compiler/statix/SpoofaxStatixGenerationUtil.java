package mb.spoofax.lwb.compiler.statix;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.task.StatixPrettyPrint;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;

public class SpoofaxStatixGenerationUtil {
    private final StatixPrettyPrint prettyPrint;

    @Inject public SpoofaxStatixGenerationUtil(StatixPrettyPrint prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void writePrettyPrintedFile(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> supplier
    ) throws Exception {
        final Result<IStrategoTerm, ? extends Exception> result = context.require(supplier);
        final IStrategoTerm ast = result.unwrap();
        final String moduleName = TermUtils.toJavaStringAt(ast, 0); // TODO: check whether this is correct for statix.

        final Result<IStrategoTerm, ? extends Exception> prettyPrintedResult = context.require(prettyPrint, supplier);
        final IStrategoTerm prettyPrintedTerm = prettyPrintedResult.unwrap();
        final String prettyPrinted = TermUtils.toJavaString(prettyPrintedTerm);

        final HierarchicalResource file = context.getHierarchicalResource(generatesSourcesDirectory.appendRelativePath(moduleName).appendToLeaf(".stx"));
        file.ensureFileExists();
        file.writeString(prettyPrinted);
        context.provide(file, ResourceStampers.hashFile());
    }
}
