package mb.jsglr1.pie;

import mb.common.message.Messages;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Value.Immutable
public interface JSGLR1ParseTaskInput extends Serializable {
    class Builder extends ImmutableJSGLR1ParseTaskInput.Builder {
        private final @Nullable JSGLR1ParseTaskDef parse;


        public Builder() {
            this.parse = null;
        }

        public Builder(JSGLR1ParseTaskDef parse) {
            this.parse = parse;
        }


        public Builder fileHint(ResourceKey file) {
            return fileHint(Option.ofSome(file));
        }


        public Builder withFile(ResourceKey file, @Nullable ResourceStamper<ReadableResource> stamper, Charset charset) {
            stringSupplier(new ResourceStringSupplier(file, stamper, charset));
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


        public Supplier<Result<JSGLR1ParseOutput, JSGLR1ParseException>> buildSupplier() {
            checkBuildPossible();
            return parse.createSupplier(build());
        }

        public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> buildRecoverableAstSupplier() {
            checkBuildPossible();
            return parse.createRecoverableAstSupplier(build());
        }

        public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> buildAstSupplier() {
            checkBuildPossible();
            return parse.createAstSupplier(build());
        }

        public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> buildRecoverableTokensSupplier() {
            checkBuildPossible();
            return parse.createRecoverableTokensSupplier(build());
        }

        public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> buildTokensSupplier() {
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

    static Builder builder(JSGLR1ParseTaskDef parse) {
        return new Builder(parse);
    }


    Supplier<String> stringSupplier();

    Optional<String> startSymbol();

    @Value.Default default Option<ResourceKey> fileHint() {
        final Supplier<String> stringSupplier = stringSupplier();
        if(stringSupplier instanceof ResourceStringSupplier) {
            return Option.ofSome(((ResourceStringSupplier)stringSupplier).key);
        } else {
            return Option.ofNone();
        }
    }

    Optional<ResourcePath> rootDirectoryHint();
}
