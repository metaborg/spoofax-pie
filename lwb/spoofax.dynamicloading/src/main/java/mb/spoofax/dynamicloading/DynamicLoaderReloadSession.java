package mb.spoofax.dynamicloading;

import mb.pie.api.ExecException;
import mb.pie.api.TopDownSession;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;

/**
 * A session for dynamically (re)loading specific languages with {@link #reload}. Mimics the design of {@link
 * TopDownSession PIE's TopDownSession}.
 */
public class DynamicLoaderReloadSession extends DynamicLoaderSession {
    private final TopDownSession session;

    public DynamicLoaderReloadSession(TopDownSession session, DynamicLoader dynamicLoader, DynamicLoad dynamicLoad) {
        super(session, dynamicLoader, dynamicLoad);
        this.session = session;
    }


    /**
     * Incrementally (re)compiles, and (re)loads the compiled language with given {@code id} and {@code compilerInput}.
     *
     * @return Dynamically (re)loaded language.
     */
    public DynamicLanguage reload(String id, CompileToJavaClassFiles.Input compilerInput) throws ExecException, InterruptedException {
        return session.require(createTask(id, compilerInput)).getValue();
    }
}
