package mb.spoofax.core.language.command.arg;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Param {
    private final String id;
    private final Class<? extends Serializable> type;
    private final boolean required;
    private final @Nullable ArgConverter<?> converter;
    private final ListView<ArgProvider> providers;


    public Param(String id, Class<? extends Serializable> type, boolean required, @Nullable ArgConverter<?> converter, ListView<ArgProvider> providers) {
        this.id = id;
        this.type = type;
        this.required = required;
        this.converter = converter;
        this.providers = providers;
    }


    public static Param of(String id, Class<? extends Serializable> type) {
        return new Param(id, type, true, null, ListView.of());
    }

    public static Param of(String id, Class<? extends Serializable> type, boolean required) {
        return new Param(id, type, required, null, ListView.of());
    }

    public static Param of(String id, Class<? extends Serializable> type, boolean required, @Nullable ArgConverter<?> converter) {
        return new Param(id, type, required, converter, ListView.of());
    }

    public static Param of(String id, Class<? extends Serializable> type, boolean required, ArgProvider provider) {
        return new Param(id, type, required, null, ListView.of(provider));
    }

    public static Param of(String id, Class<? extends Serializable> type, boolean required, ListView<ArgProvider> providers) {
        return new Param(id, type, required, null, providers);
    }

    public static Param of(String id, Class<? extends Serializable> type, boolean required, ArgProvider... providers) {
        return new Param(id, type, required, null, ListView.of(providers));
    }

    public static Param of(String id, Class<? extends Serializable> type, boolean required, @Nullable ArgConverter<?> converter, ArgProvider provider) {
        return new Param(id, type, required, converter, ListView.of(provider));
    }

    public static Param of(String id, Class<? extends Serializable> type, boolean required, @Nullable ArgConverter<?> converter, ListView<ArgProvider> providers) {
        return new Param(id, type, required, converter, providers);
    }

    public static Param of(String id, Class<? extends Serializable> type, boolean required, @Nullable ArgConverter<?> converter, ArgProvider... providers) {
        return new Param(id, type, required, converter, ListView.of(providers));
    }


    public String getId() {
        return id;
    }

    public Class<? extends Serializable> getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public @Nullable ArgConverter<?> getConverter() {
        return converter;
    }

    public ListView<ArgProvider> getProviders() {
        return providers;
    }

    public EnumSetView<CommandContextType> getUsedContextTypes() {
        return EnumSetView.of(providers.stream()
            .flatMap(provider -> provider.caseOf()
                .context(type -> type)
                .otherwiseEmpty()
                .map(Stream::of)
                .orElseGet(Stream::empty)
            )
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(CommandContextType.class)))
        );
    }

    public EnumSetView<EnclosingCommandContextType> getUsedEnclosingContextTypes() {
        return EnumSetView.of(providers.stream()
            .flatMap(provider -> provider.caseOf()
                .enclosingContext(type -> type)
                .otherwiseEmpty()
                .map(Stream::of)
                .orElseGet(Stream::empty)
            )
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(EnclosingCommandContextType.class)))
        );
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final Param other = (Param)obj;
        return required == other.required &&
            id.equals(other.id) &&
            type.equals(other.type) &&
            Objects.equals(converter, other.converter) &&
            providers.equals(other.providers);
    }

    @Override public int hashCode() {
        return Objects.hash(id, type, required, converter, providers);
    }

    @Override public String toString() {
        return "Param{" +
            "id='" + id + '\'' +
            ", type=" + type +
            ", required=" + required +
            ", converter=" + converter +
            ", providers=" + providers +
            '}';
    }
}
