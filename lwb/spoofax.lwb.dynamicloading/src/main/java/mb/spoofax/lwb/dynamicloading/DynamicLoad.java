package mb.spoofax.lwb.dynamicloading;

import mb.common.result.Result;
import mb.common.util.SetView;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientImpl;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.stream.Stream;

@DynamicLoadingScope
public class DynamicLoad implements TaskDef<Supplier<Result<DynamicLoad.SupplierOutput, ?>>, OutTransient<Result<DynamicComponent, DynamicLoadException>>> {
    public static class SupplierOutput implements Serializable {
        public final ResourcePath rootDirectory;
        public final SetView<ResourcePath> javaClassPaths;
        public final String participantClassQualifiedId;

        public SupplierOutput(
            ResourcePath rootDirectory,
            SetView<ResourcePath> javaClassPaths,
            String participantClassQualifiedId
        ) {
            this.rootDirectory = rootDirectory;
            this.javaClassPaths = javaClassPaths;
            this.participantClassQualifiedId = participantClassQualifiedId;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final SupplierOutput that = (SupplierOutput)o;
            if(!rootDirectory.equals(that.rootDirectory)) return false;
            if(!javaClassPaths.equals(that.javaClassPaths)) return false;
            return participantClassQualifiedId.equals(that.participantClassQualifiedId);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + javaClassPaths.hashCode();
            result = 31 * result + participantClassQualifiedId.hashCode();
            return result;
        }

        @Override public String toString() {
            return "DynamicLoad$SupplierOutput{" +
                "rootDirectory=" + rootDirectory +
                ", javaClassPaths=" + javaClassPaths +
                ", participantClassQualifiedId='" + participantClassQualifiedId + '\'' +
                '}';
        }
    }


    private final DynamicComponentManager dynamicComponentManager;


    @Inject public DynamicLoad(DynamicComponentManager dynamicComponentManager) {
        this.dynamicComponentManager = dynamicComponentManager;
    }


    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public OutTransient<Result<DynamicComponent, DynamicLoadException>> exec(ExecContext context, Supplier<Result<DynamicLoad.SupplierOutput, ?>> input) {
        final Result<DynamicComponent, DynamicLoadException> result = context.require(input)
            .mapErr(DynamicLoadException::supplyDynamicLoadInfoFail)
            .flatMap(o -> run(context, o));
        return new OutTransientImpl<>(result, true);
    }

    @Override
    public boolean shouldExecWhenAffected(Supplier<Result<DynamicLoad.SupplierOutput, ?>> input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<DynamicComponent, DynamicLoadException> run(ExecContext context, SupplierOutput supplierOutput) {
        try {
            for(ResourcePath path : supplierOutput.javaClassPaths) {
                // HACK: create dependency to each file separately, instead of one for the directory, to ensure this task
                //       gets re-executed in a bottom-up build when any file changes
                try(Stream<? extends ReadableResource> files = context.require(path).walk(ResourceWalker.ofTrue(), ResourceMatcher.ofFile())) {
                    for(ReadableResource file : new StreamIterable<>(files)) {
                        context.require(file, ResourceStampers.modifiedFile());
                    }
                }
            }
            return dynamicComponentManager.loadOrReloadFromCompiledSources(supplierOutput.rootDirectory, supplierOutput.javaClassPaths, supplierOutput.participantClassQualifiedId);
        } catch(MalformedURLException e) {
            return Result.ofErr(DynamicLoadException.classPathToUrlFail(e));
        } catch(IOException e) {
            return Result.ofErr(DynamicLoadException.requireInputFileFail(e));
        } catch(ReflectiveOperationException e) {
            return Result.ofErr(DynamicLoadException.participantInstantiateFail(e));
        }
    }
}
