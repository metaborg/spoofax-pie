package mb.spoofax.lwb.compiler;

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
public class SpoofaxLwbCompilerJavaModule {
    private final JavaCompiler compiler;
    private final FileManagerFactory fileManagerFactory;
    private final JavaFileObjectFactory javaFileObjectFactory;

    public SpoofaxLwbCompilerJavaModule(JavaCompiler compiler, FileManagerFactory fileManagerFactory, JavaFileObjectFactory javaFileObjectFactory) {
        this.compiler = compiler;
        this.fileManagerFactory = fileManagerFactory;
        this.javaFileObjectFactory = javaFileObjectFactory;
    }

    public SpoofaxLwbCompilerJavaModule() {
        this(ToolProvider.getSystemJavaCompiler(), JavaResourceManager::new, new JavaResource.Factory());
    }

    @Provides @SpoofaxLwbCompilerScope
    CompileJava provideCompileJava() {
        return new CompileJava(new JdkJavaCompiler(compiler, fileManagerFactory, javaFileObjectFactory));
    }
}
