package mb.cfg.task;

import mb.cfg.CfgScope;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.jsglr.common.JsglrParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
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
import java.util.Set;

@CfgScope
public class CfgRootDirectoryToObject implements TaskDef<ResourcePath, Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> {
    public static final String cfgFileRelativePath = "spoofaxc.cfg";
    public static final String lockFileRelativePath = "spoofaxc.lock";

    private final ResourceService resourceService;
    private final CfgParse parse;
    private final CfgAnalyze analyze;
    private final CfgToObject toObject;

    @Inject
    public CfgRootDirectoryToObject(
        ResourceService resourceService,
        CfgParse parse,
        CfgAnalyze analyze,
        CfgToObject toObject
    ) {
        this.resourceService = resourceService;
        this.parse = parse;
        this.analyze = analyze;
        this.toObject = toObject;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<CfgToObject.Output, CfgRootDirectoryToObjectException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        final ResourcePath cfgFile = rootDirectory.appendRelativePath(cfgFileRelativePath);
        final Supplier<Result<IStrategoTerm, JsglrParseException>> astSupplier = parse.inputBuilder().withFile(cfgFile).rootDirectoryHint(rootDirectory).buildAstSupplier();
        final Supplier<Result<CfgAnalyze.Output, ?>> analyzeOutputSupplier = analyze.createSupplier(new CfgAnalyze.Input(cfgFile, astSupplier));
        final ResourcePath lockFilePath = rootDirectory.appendRelativePath(lockFileRelativePath);
        final WritableResource lockFile = resourceService.getWritableResource(lockFilePath);
        final Supplier<Result<Properties, IOException>> propertiesSupplier = new PropertiesSupplier(lockFilePath);
        return context.require(toObject, new CfgToObject.Input(rootDirectory, cfgFile, analyzeOutputSupplier, propertiesSupplier))
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
        private final ResourcePath lockFilePath;

        public PropertiesSupplier(ResourcePath lockFilePath) {
            this.lockFilePath = lockFilePath;
        }

        @Override public Result<Properties, IOException> get(ExecContext ctx) {
            final Properties properties = new Properties();
            try {
                final ReadableResource lockFile = ctx.require(lockFilePath);
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
            return lockFilePath.equals(that.lockFilePath);
        }

        @Override public int hashCode() {
            return lockFilePath.hashCode();
        }

        @Override public String toString() {
            return "PropertiesSupplier{" +
                "lockFilePath=" + lockFilePath +
                '}';
        }
    }
}
