package mb.spoofax.runtime.stratego;

import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.cfg.StrategoCompilerConfig;
import mb.spoofax.runtime.util.Arguments;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.*;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;

import javax.annotation.Nullable;
import java.io.IOException;

public class StrategoCompiler {
    public class Result {
        public final PPath outputFile;
        public final PPath depFile;


        public Result(PPath outputFile, PPath depFile) {
            this.outputFile = outputFile;
            this.depFile = depFile;
        }
    }

    public @Nullable Result compile(StrategoCompilerConfig config) throws IOException, StrategoException {
        final PPath mainFile = config.mainFile();
        final PPath outputFile = config.outputFileOrDefault();
        final Iterable<PPath> includeDirs = config.includeDirs();
        final Iterable<PPath> includeFiles = config.includeFiles();
        final Iterable<String> includeLibs = config.includeLibs();
        final PPath baseDir = config.baseDirOrDefault();
        if(baseDir == null) {
            throw new RuntimeException(
                "Cannot compile Stratego code; base directory was not set, and main file " + mainFile + " has no parent directory to use as default");
        }
        final PPath cacheDir = config.cacheDirOrDefault();
        if(cacheDir == null) {
            throw new RuntimeException(
                "Cannot compile Stratego code; cache directory was not set, and main file " + mainFile + " has no parent directory to use as default");
        }

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
