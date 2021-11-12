package mb.spoofax.lwb.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import mb.pie.task.java.CompileJava;
import mb.pie.task.java.jdk.FileManagerFactory;
import mb.pie.task.java.jdk.JavaFileObjectFactory;
import mb.pie.task.java.jdk.JavaResource;
import mb.pie.task.java.jdk.JavaResourceManager;
import mb.pie.task.java.jdk.JdkJavaCompiler;

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
        return new CompileJava(new JdkJavaCompiler(compiler, fileManagerFactory, javaFileObjectFactory));
    }
}
