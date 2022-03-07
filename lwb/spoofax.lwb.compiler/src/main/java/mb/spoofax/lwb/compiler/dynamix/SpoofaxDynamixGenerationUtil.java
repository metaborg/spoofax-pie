package mb.spoofax.lwb.compiler.dynamix;

import mb.common.result.Result;
import mb.dynamix.task.DynamixPrettyPrint;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;

/**
 * Dynamix generation utilities in the context of the Spoofax LWB compiler.
 */
public class SpoofaxDynamixGenerationUtil {
    private final DynamixPrettyPrint prettyPrint;

    @Inject public SpoofaxDynamixGenerationUtil(DynamixPrettyPrint prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void writePrettyPrintedFile(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> supplier
    ) throws Exception {
        final Result<IStrategoTerm, ? extends Exception> result = context.require(supplier);
        final IStrategoTerm ast = result.unwrap();
        final String moduleName = TermUtils.toJavaStringAt(ast, 0); // output is `Program(mid, contents)`, extract mid

        final Result<IStrategoTerm, ? extends Exception> prettyPrintedResult = context.require(prettyPrint, supplier);
        final IStrategoTerm prettyPrintedTerm = prettyPrintedResult.unwrap();
        final String prettyPrinted = TermUtils.toJavaString(prettyPrintedTerm);

        final HierarchicalResource file = context.getHierarchicalResource(generatesSourcesDirectory.appendRelativePath(moduleName).appendToLeaf(".dx"));
        file.ensureFileExists();
        file.writeString(prettyPrinted);
        context.provide(file, ResourceStampers.hashFile());
    }
}
