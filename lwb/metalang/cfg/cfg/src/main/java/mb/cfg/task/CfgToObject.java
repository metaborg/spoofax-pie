package mb.cfg.task;

import mb.cfg.CfgScope;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageToJavaClassPathInput;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;

@CfgScope
public class CfgToObject implements TaskDef<CfgToObject.Input, Result<CfgToObject.Output, CfgToObjectException>> {
    public static class Input implements Serializable {
        public final ResourcePath rootDirectory;
        public final @Nullable ResourceKey cfgResource;
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
        public final Supplier<? extends Result<Properties, ?>> propertiesSupplier;

        public Input(
            ResourcePath rootDirectory,
            @Nullable ResourceKey cfgResource,
            Supplier<? extends Result<IStrategoTerm, ?>> astSupplier,
            Supplier<? extends Result<Properties, ?>> propertiesSupplier
        ) {
            this.rootDirectory = rootDirectory;
            this.cfgResource = cfgResource;
            this.astSupplier = astSupplier;
            this.propertiesSupplier = propertiesSupplier;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            if(cfgResource != null ? !cfgResource.equals(input.cfgResource) : input.cfgResource != null) return false;
            if(!astSupplier.equals(input.astSupplier)) return false;
            return propertiesSupplier.equals(input.propertiesSupplier);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + (cfgResource != null ? cfgResource.hashCode() : 0);
            result = 31 * result + astSupplier.hashCode();
            result = 31 * result + propertiesSupplier.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CfgToObject$Input{" +
                "rootDirectory=" + rootDirectory +
                ", cfgResource=" + cfgResource +
                ", astSupplier=" + astSupplier +
                ", propertiesSupplier=" + propertiesSupplier +
                '}';
        }
    }

    public static class Output implements Serializable {
        public final KeyedMessages messages;
        public final CompileLanguageToJavaClassPathInput compileLanguageToJavaClassPathInput;
        public final Properties properties;

        public Output(
            KeyedMessages messages,
            CompileLanguageToJavaClassPathInput compileLanguageToJavaClassPathInput,
            Properties properties
        ) {
            this.messages = messages;
            this.compileLanguageToJavaClassPathInput = compileLanguageToJavaClassPathInput;
            this.properties = properties;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Output output = (Output)o;
            if(!messages.equals(output.messages)) return false;
            if(!compileLanguageToJavaClassPathInput.equals(output.compileLanguageToJavaClassPathInput)) return false;
            return properties.equals(output.properties);
        }

        @Override public int hashCode() {
            int result = messages.hashCode();
            result = 31 * result + compileLanguageToJavaClassPathInput.hashCode();
            result = 31 * result + properties.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CfgToObject$Output{" +
                "messages=" + messages +
                ", compileLanguageToJavaClassPathInput=" + compileLanguageToJavaClassPathInput +
                ", properties=" + properties +
                '}';
        }
    }


    @Inject
    public CfgToObject() {
        // Default constructor required for injection.
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Output, CfgToObjectException> exec(ExecContext context, CfgToObject.Input input) {
        return context.require(input.astSupplier)
            .mapErr(CfgToObjectException::astSupplyFail)
            .flatMap(ast -> context.require(input.propertiesSupplier)
                .mapErr(CfgToObjectException::propertiesSupplyFail)
                .flatMap(properties -> toOutput(input.rootDirectory, input.cfgResource, ast, properties))
            );
    }

    private Result<Output, CfgToObjectException> toOutput(
        ResourcePath rootDirectory,
        @Nullable ResourceKey cfgFile,
        IStrategoTerm ast,
        Properties properties
    ) throws InvalidAstShapeException {
        final AstToObject.Output output;
        try {
            output = AstToObject.convert(rootDirectory, cfgFile, ast, properties);
        } catch(IllegalStateException e) {
            return Result.ofErr(CfgToObjectException.buildConfigObjectFail(e));
        }
        if(output.messages.containsError()) {
            return Result.ofErr(CfgToObjectException.validationFail(output.messages));
        } else {
            return Result.ofOk(new Output(output.messages, output.compileLanguageToJavaClassPathInput, output.properties));
        }
    }
}
