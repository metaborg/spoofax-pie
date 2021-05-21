package mb.cfg.task;

import mb.cfg.CfgScope;
import mb.cfg.CompileLanguageInput;
import mb.cfg.CompileLanguageInputCustomizer;
import mb.cfg.convert.CfgAstToObject;
import mb.cfg.convert.InvalidAstShapeException;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;

@CfgScope
public class CfgToObject implements TaskDef<CfgToObject.Input, Result<CfgToObject.Output, CfgToObjectException>> {
    public static class Input implements Serializable {
        public final ResourcePath rootDirectory;
        public final @Nullable ResourceKey cfgResource;
        public final Supplier<? extends Result<CfgAnalyze.Output, ?>> analysisOutputSupplier;
        public final Supplier<? extends Result<Properties, ?>> propertiesSupplier;

        public Input(
            ResourcePath rootDirectory,
            @Nullable ResourceKey cfgResource,
            Supplier<? extends Result<CfgAnalyze.Output, ?>> analysisOutputSupplier,
            Supplier<? extends Result<Properties, ?>> propertiesSupplier
        ) {
            this.rootDirectory = rootDirectory;
            this.cfgResource = cfgResource;
            this.analysisOutputSupplier = analysisOutputSupplier;
            this.propertiesSupplier = propertiesSupplier;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            if(cfgResource != null ? !cfgResource.equals(input.cfgResource) : input.cfgResource != null) return false;
            if(!analysisOutputSupplier.equals(input.analysisOutputSupplier)) return false;
            return propertiesSupplier.equals(input.propertiesSupplier);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + (cfgResource != null ? cfgResource.hashCode() : 0);
            result = 31 * result + analysisOutputSupplier.hashCode();
            result = 31 * result + propertiesSupplier.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CfgToObject$Input{" +
                "rootDirectory=" + rootDirectory +
                ", cfgResource=" + cfgResource +
                ", astSupplier=" + analysisOutputSupplier +
                ", propertiesSupplier=" + propertiesSupplier +
                '}';
        }
    }

    public static class Output implements Serializable {
        public final KeyedMessages messages;
        public final CompileLanguageInput compileLanguageInput;
        public final Properties properties;

        public Output(
            KeyedMessages messages,
            CompileLanguageInput compileLanguageInput,
            Properties properties
        ) {
            this.messages = messages;
            this.compileLanguageInput = compileLanguageInput;
            this.properties = properties;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Output output = (Output)o;
            if(!messages.equals(output.messages)) return false;
            if(!compileLanguageInput.equals(output.compileLanguageInput)) return false;
            return properties.equals(output.properties);
        }

        @Override public int hashCode() {
            int result = messages.hashCode();
            result = 31 * result + compileLanguageInput.hashCode();
            result = 31 * result + properties.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CfgToObject$Output{" +
                "messages=" + messages +
                ", compileLanguageToJavaClassPathInput=" + compileLanguageInput +
                ", properties=" + properties +
                '}';
        }
    }


    private final CfgGetStrategoRuntimeProvider getStrategoRuntimeProvider;
    private final CompileLanguageInputCustomizer customizer;


    @Inject
    public CfgToObject(CfgGetStrategoRuntimeProvider getStrategoRuntimeProvider, CompileLanguageInputCustomizer customizer) {
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
        this.customizer = customizer;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Output, CfgToObjectException> exec(ExecContext context, CfgToObject.Input input) {
        return context.require(input.analysisOutputSupplier)
            .mapErr(CfgToObjectException::analyzeExceptionalFail)
            .flatMap(analysisOutput -> context.require(input.propertiesSupplier)
                .mapErr(CfgToObjectException::analyzeExceptionalFail)
                .flatMap(properties -> toOutput(context, input.rootDirectory, input.cfgResource, analysisOutput, properties))
            );
    }

    private Result<Output, CfgToObjectException> toOutput(
        ExecContext context,
        ResourcePath rootDirectory,
        @Nullable ResourceKey cfgFile,
        CfgAnalyze.Output analysisOutput,
        Properties properties
    ) throws InvalidAstShapeException {
        if(analysisOutput.result.messages.containsError()) {
            final KeyedMessages messages;
            if(cfgFile != null) {
                messages = analysisOutput.result.messages.toKeyed(cfgFile);
            } else {
                messages = analysisOutput.result.messages.toKeyed();
            }
            return Result.ofErr(CfgToObjectException.analyzeFail(messages));
        }

        final Provider<StrategoRuntime> strategoRuntimeProvider = context.require(getStrategoRuntimeProvider, None.instance).getValue();
        final IStrategoTerm ast;
        try {
            ast = strategoRuntimeProvider.get().addContextObject(analysisOutput.context).invoke("normalize", analysisOutput.result.ast);
        } catch(StrategoException e) {
            return Result.ofErr(CfgToObjectException.normalizationFail(e));
        }

        final CfgAstToObject.Output output;
        try {
            output = CfgAstToObject.convert(rootDirectory, cfgFile, ast, properties, customizer);
        } catch(IllegalStateException e) {
            return Result.ofErr(CfgToObjectException.buildConfigObjectFail(e));
        }
        if(output.messages.containsError()) {
            return Result.ofErr(CfgToObjectException.validationFail(output.messages));
        } else {
            return Result.ofOk(new Output(output.messages, output.compileLanguageInput, output.properties));
        }
    }
}
