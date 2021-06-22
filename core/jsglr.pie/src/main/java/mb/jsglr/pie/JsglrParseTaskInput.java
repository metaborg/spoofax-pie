package mb.jsglr.pie;

import mb.common.message.Messages;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.text.Text;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.resource.ResourceTextSupplier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Value.Immutable
public interface JsglrParseTaskInput extends Serializable {
    class Builder extends ImmutableJsglrParseTaskInput.Builder {
        private final @Nullable JsglrParseTaskDef parse;


        public Builder() {
            this.parse = null;
        }

        public Builder(JsglrParseTaskDef parse) {
            this.parse = parse;
        }


        public Builder fileHint(ResourceKey file) {
            return fileHint(Option.ofSome(file));
        }


        public Builder withFile(ResourceKey file, @Nullable ResourceStamper<ReadableResource> stamper, Charset charset) {
            textSupplier(new ResourceTextSupplier(file, stamper, charset));
            return fileHint(Option.ofSome(file));
        }

        public Builder withFile(ResourceKey file, ResourceStamper<ReadableResource> stamper) {
            return withFile(file, stamper, StandardCharsets.UTF_8);
        }

        public Builder withFile(ResourceKey file, Charset charset) {
            return withFile(file, null, charset);
        }

        public Builder withFile(ResourceKey file) {
            return withFile(file, StandardCharsets.UTF_8);
        }


        public Supplier<Result<JsglrParseOutput, JsglrParseException>> buildSupplier() {
            checkBuildPossible();
            return parse.createSupplier(build());
        }

        public Supplier<Result<IStrategoTerm, JsglrParseException>> buildRecoverableAstSupplier() {
            checkBuildPossible();
            return parse.createRecoverableAstSupplier(build());
        }

        public Supplier<Result<IStrategoTerm, JsglrParseException>> buildAstSupplier() {
            checkBuildPossible();
            return parse.createAstSupplier(build());
        }

        public Supplier<Result<JSGLRTokens, JsglrParseException>> buildRecoverableTokensSupplier() {
            checkBuildPossible();
            return parse.createRecoverableTokensSupplier(build());
        }

        public Supplier<Result<JSGLRTokens, JsglrParseException>> buildTokensSupplier() {
            checkBuildPossible();
            return parse.createTokensSupplier(build());
        }

        public Supplier<Messages> buildMessagesSupplier() {
            return parse.createMessagesSupplier(build());
        }


        private void checkBuildPossible() {
            if(parse == null)
                throw new IllegalStateException("Cannot build supplier; parse task definition was not passed into builder");
        }
    }

    static Builder builder() {
        return new Builder();
    }

    static Builder builder(JsglrParseTaskDef parse) {
        return new Builder(parse);
    }


    Supplier<Text> textSupplier();

    Optional<String> startSymbol();

    @Value.Default default Option<ResourceKey> fileHint() {
        final Supplier<Text> supplier = textSupplier();
        if(supplier instanceof ResourceTextSupplier) {
            return Option.ofSome(((ResourceTextSupplier)supplier).key);
        } else {
            return Option.ofNone();
        }
    }

    Optional<ResourcePath> rootDirectoryHint();
}
