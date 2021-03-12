package mb.cfg.task;

import mb.cfg.CfgScope;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;

@CfgScope
public class CfgRootDirectoryToObject implements TaskDef<ResourcePath, Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> {
    private final ResourceService resourceService;
    private final CfgParse parse;
    private final CfgToObject toObject;

    @Inject
    public CfgRootDirectoryToObject(ResourceService resourceService, CfgParse parse, CfgToObject toObject) {
        this.resourceService = resourceService;
        this.parse = parse;
        this.toObject = toObject;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<CfgToObject.Output, CfgRootDirectoryToObjectException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        final ResourcePath cfgFile = rootDirectory.appendRelativePath("spoofaxc.cfg");
        final Supplier<Result<IStrategoTerm, JSGLR1ParseException>> astSupplier = parse.createAstSupplier(cfgFile);
        final ResourcePath lockFilePath = rootDirectory.appendRelativePath("spoofaxc.lock");
        final WritableResource lockFile = resourceService.getWritableResource(lockFilePath);
        final Supplier<Result<Properties, IOException>> propertiesSupplier = new PropertiesSupplier(lockFile);
        return context.require(toObject, new CfgToObject.Input(rootDirectory, cfgFile, astSupplier, propertiesSupplier))
            .mapErr(e -> CfgRootDirectoryToObjectException.convertFail(e, cfgFile, lockFilePath))
            .flatMap(output -> {
                try {
                    try(final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(lockFile.openWrite()))) {
                        output.properties.storeWithoutDate(bufferedWriter);
                    }
                    // Require lockfile after writing. If we would require before writing, this task would re-execute
                    // itself infinitely when the lockfile is changed. Now it is only re-executed when the lockfile is
                    // manually changed, which is what we desire.
                    context.require(lockFile);
                } catch(IOException e) {
                    return Result.ofErr(CfgRootDirectoryToObjectException.lockFileWriteFail(e, cfgFile, lockFilePath));
                }
                return Result.ofOk(output);
            });
    }

    private static class PropertiesSupplier implements Supplier<Result<Properties, IOException>>, Serializable {
        private final ReadableResource lockFile;

        public PropertiesSupplier(ReadableResource lockFile) {
            this.lockFile = lockFile;
        }

        @Override public Result<Properties, IOException> get(ExecContext ctx) {
            final Properties properties = new Properties();
            try {
                if(lockFile.exists()) {
                    try(final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(lockFile.openRead()))) {
                        properties.load(bufferedReader);
                    }
                }
            } catch(IOException e) {
                return Result.ofErr(e);
            }
            return Result.ofOk(properties);
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final PropertiesSupplier that = (PropertiesSupplier)o;
            return lockFile.equals(that.lockFile);
        }

        @Override public int hashCode() {
            return lockFile.hashCode();
        }

        @Override public String toString() {
            return "PropertiesSupplier{" +
                "lockFile=" + lockFile +
                '}';
        }
    }
}
