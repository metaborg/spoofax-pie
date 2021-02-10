package mb.spoofax.dynamicloading;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientImpl;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

public class DynamicLoad implements TaskDef<DynamicLoad.Input, OutTransient<DynamicLanguage>> {
    public static class Input implements Serializable {
        public final String id;
        public final CompileToJavaClassFiles.Input compilerInput;

        public Input(String id, CompileToJavaClassFiles.Input compilerInput) {
            this.id = id;
            this.compilerInput = compilerInput;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!id.equals(input.id)) return false;
            return compilerInput.equals(input.compilerInput);
        }

        @Override public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + compilerInput.hashCode();
            return result;
        }

        @Override public String toString() {
            return "DynamicLoad.Input{" +
                "id='" + id + '\'' +
                ", compilerInput=" + compilerInput +
                '}';
        }
    }

    private final ResourceServiceComponent resourceServiceComponent;
    private final PlatformComponent platformComponent;
    private final CompileToJavaClassFiles compiler;
    private final DynamicLoader dynamicLoader;

    public DynamicLoad(
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,
        CompileToJavaClassFiles compiler,
        DynamicLoader dynamicLoader
    ) {
        this.resourceServiceComponent = resourceServiceComponent;
        this.platformComponent = platformComponent;
        this.compiler = compiler;
        this.dynamicLoader = dynamicLoader;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public Serializable key(Input input) {
        return input.id;
    }

    @Override
    public OutTransient<DynamicLanguage> exec(ExecContext context, Input input) throws Exception {
        final CompileToJavaClassFiles.Input compilerInput = input.compilerInput;
        final Result<CompileToJavaClassFiles.Output, CompileToJavaClassFiles.CompileException> result = context.require(compiler, compilerInput);
        // TODO: properly handle error
        final CompileToJavaClassFiles.Output output = result.unwrap();
        final ArrayList<URL> classPath = new ArrayList<>();
        for(ResourcePath path : output.classPath()) {
            // TODO: properly handle error
            context.require(path, ResourceStampers.hashDirRec(new TrueResourceWalker(), new FileResourceMatcher()));
            final @Nullable File file = context.getResourceService().toLocalFile(path);
            if(file == null) {
                // TODO: properly handle error
                throw new IOException("Cannot dynamically load language; resource at path '" + path + "' is not on the local filesystem, and can therefore not be loaded into a URLClassLoader");
            }
            classPath.add(file.toURI().toURL());
        }
        final DynamicLanguage dynamicLanguage = new DynamicLanguage(
            classPath.toArray(new URL[0]),
            compilerInput.adapterProjectInput().daggerResourcesComponent().qualifiedId(),
            compilerInput.adapterProjectInput().daggerComponent().qualifiedId(),
            compilerInput.adapterProjectInput().resourcesComponent().qualifiedId(),
            compilerInput.adapterProjectInput().resourcesComponent().idAsCamelCase(),
            resourceServiceComponent,
            platformComponent
        );
        dynamicLoader.register(input.id, dynamicLanguage);
        return new OutTransientImpl<>(dynamicLanguage, true);
    }
}
