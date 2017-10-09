package mb.spoofax.runtime.impl.stratego;

import java.io.IOException;

import javax.annotation.Nullable;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;

import mb.spoofax.runtime.impl.cfg.StrategoConfig;
import mb.spoofax.runtime.impl.util.Arguments;
import mb.vfs.path.PPath;

public class StrategoCompiler {
    public class Result {
        public final PPath outputFile;
        public final PPath depFile;


        public Result(PPath outputFile, PPath depFile) {
            this.outputFile = outputFile;
            this.depFile = depFile;
        }
    }

    public @Nullable Result compile(StrategoConfig config) throws IOException, StrategoException {
        final PPath mainFile = config.mainFile();
        final PPath outputFile = config.outputFile();
        final Iterable<PPath> includeDirs = config.includeDirs();
        final Iterable<PPath> includeFiles = config.includeFiles();
        final Iterable<String> includeLibs = config.includeLibs();
        final PPath baseDir = config.baseDir();
        final PPath cacheDir = config.cacheDir();

        // Create necessary directories
        outputFile.createParentDirectories();
        cacheDir.createDirectories();
        
        // Delete rtree file (if it exists) to prevent it from influencing the build.
        outputFile.replaceExtension("rtree").deleteFile();

        final Arguments arguments =
            new Arguments().addPath("-i", mainFile).addPath("-o", outputFile).add("--library").add("--clean").add("-F");
        includeFiles.forEach(path -> arguments.addPath("-i", path));
        includeDirs.forEach(path -> arguments.addPath("-I", path));
        includeLibs.forEach(path -> arguments.add("-la", path));
        arguments.addPath("--cache-dir", cacheDir);
        final String[] args = arguments.asStrings(null).toArray(new String[0]);

        final IOAgent agent = new IOAgent();
        agent.setWorkingDir(baseDir.toString());
        agent.setDefinitionDir(baseDir.toString());

        final Context context = org.strategoxt.strj.strj.init();
        final ITermFactory factory = context.getFactory();
        context.setIOAgent(agent);

        try {
            dr_scope_all_start_0_0.instance.invoke(context, factory.makeTuple());
            context.invokeStrategyCLI(org.strategoxt.strj.main_0_0.instance, "strj", args);
        } catch(StrategoExit e) {
            if(e.getValue() != StrategoExit.SUCCESS) {
                throw e;
            }
        } finally {
            dr_scope_all_end_0_0.instance.invoke(context, factory.makeTuple());
        }

        final PPath depFile = outputFile.extend(".dep");

        return new Result(outputFile, depFile);
    }
}
