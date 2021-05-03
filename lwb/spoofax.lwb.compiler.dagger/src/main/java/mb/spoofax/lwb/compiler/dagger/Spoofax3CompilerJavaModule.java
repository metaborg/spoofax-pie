package mb.spoofax.lwb.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import mb.pie.task.java.CompileJava;
import mb.pie.task.java.FileManagerFactory;
import mb.pie.task.java.JavaFileObjectFactory;
import mb.pie.task.java.JavaResource;
import mb.pie.task.java.JavaResourceManager;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

@Module
public class Spoofax3CompilerJavaModule {
    private final JavaCompiler compiler;
    private final FileManagerFactory fileManagerFactory;
    private final JavaFileObjectFactory javaFileObjectFactory;

    public Spoofax3CompilerJavaModule(JavaCompiler compiler, FileManagerFactory fileManagerFactory, JavaFileObjectFactory javaFileObjectFactory) {
        this.compiler = compiler;
        this.fileManagerFactory = fileManagerFactory;
        this.javaFileObjectFactory = javaFileObjectFactory;
    }

    public Spoofax3CompilerJavaModule() {
        this(ToolProvider.getSystemJavaCompiler(), JavaResourceManager::new, new JavaResource.Factory());
    }

    @Provides @Spoofax3CompilerScope
    CompileJava provideCompileJava() {
        return new CompileJava(compiler, fileManagerFactory, javaFileObjectFactory);
    }
}
