package mb.spoofax.runtime.stratego;

import mb.fs.java.JavaFSNode;
import mb.fs.java.JavaFSPath;
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
        public final JavaFSNode outputFile;
        public final JavaFSNode depFile;


        public Result(JavaFSNode outputFile, JavaFSNode depFile) {
            this.outputFile = outputFile;
            this.depFile = depFile;
        }
    }

    public @Nullable Result compile(StrategoCompilerConfig config) throws IOException, StrategoException {
        final JavaFSPath mainFile = config.mainFile();
        final JavaFSNode outputFile = config.outputFileOrDefault().toNode();
        final Iterable<JavaFSPath> includeDirs = config.includeDirs();
        final Iterable<JavaFSPath> includeFiles = config.includeFiles();
        final Iterable<String> includeLibs = config.includeLibs();
        final JavaFSPath baseDir = config.baseDirOrDefault();
        if(baseDir == null) {
            throw new RuntimeException(
                "Cannot compile Stratego code; base directory was not set, and main file " + mainFile + " has no parent directory to use as default");
        }
        final JavaFSPath cacheDir = config.cacheDirOrDefault();
        if(cacheDir == null) {
            throw new RuntimeException(
                "Cannot compile Stratego code; cache directory was not set, and main file " + mainFile + " has no parent directory to use as default");
        }

        // Create necessary directories
        outputFile.createParents();
        cacheDir.toNode().createDirectory(true);

        // Delete rtree file (if it exists) to prevent it from influencing the build.
        final JavaFSNode rtreeFile = outputFile.replaceLeafExtension("rtree");
        rtreeFile.delete();

        final Arguments arguments =
            new Arguments().addPath("-i", mainFile).addPath("-o", outputFile.getPath()).add("--library").add("--clean").add("-F");
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

        final JavaFSNode depFile = outputFile.appendExtensionToLeaf("dep");

        return new Result(outputFile, depFile);
    }
}
