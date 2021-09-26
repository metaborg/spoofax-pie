package mb.statix.codecompletion;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Booleans;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Immutable implementation of {@link AbstractCompletionExpectation}.
 * <p>
 * Use the static factory method to create immutable instances:
 * {@code CompletionExpectation.of()}.
 */
@SuppressWarnings({"all"})
final class CompletionExpectation<T extends ITerm>
    extends AbstractCompletionExpectation<T> {
    private final T incompleteAst;
    private final ImmutableMap<ITermVar, ITerm> expectations;
    private final mb.statix.@Nullable SolverState state;
    private transient final Set<ITermVar> vars;
    private transient final boolean complete;

    private CompletionExpectation(
        T incompleteAst,
        Map<? extends ITermVar, ? extends ITerm> expectations,
        mb.statix.@Nullable SolverState state) {
        this.incompleteAst = Objects.requireNonNull(incompleteAst, "incompleteAst");
        this.expectations = ImmutableMap.copyOf(expectations);
        this.state = state;
        this.vars = initShim.getVars();
        this.complete = initShim.isComplete();
        this.initShim = null;
    }

    private CompletionExpectation(
        CompletionExpectation<T> original,
        T incompleteAst,
        ImmutableMap<ITermVar, ITerm> expectations,
        mb.statix.@Nullable SolverState state) {
        this.incompleteAst = incompleteAst;
        this.expectations = expectations;
        this.state = state;
        this.vars = initShim.getVars();
        this.complete = initShim.isComplete();
        this.initShim = null;
    }

    private static final byte STAGE_INITIALIZING = -1;
    private static final byte STAGE_UNINITIALIZED = 0;
    private static final byte STAGE_INITIALIZED = 1;
    private transient volatile InitShim initShim = new InitShim();

    private final class InitShim {
        private byte varsBuildStage = STAGE_UNINITIALIZED;
        private Set<ITermVar> vars;

        Set<ITermVar> getVars() {
            if (varsBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
            if (varsBuildStage == STAGE_UNINITIALIZED) {
                varsBuildStage = STAGE_INITIALIZING;
                this.vars = Objects.requireNonNull(CompletionExpectation.super.getVars(), "vars");
                varsBuildStage = STAGE_INITIALIZED;
            }
            return this.vars;
        }

        private byte completeBuildStage = STAGE_UNINITIALIZED;
        private boolean complete;

        boolean isComplete() {
            if (completeBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
            if (completeBuildStage == STAGE_UNINITIALIZED) {
                completeBuildStage = STAGE_INITIALIZING;
                this.complete = CompletionExpectation.super.isComplete();
                completeBuildStage = STAGE_INITIALIZED;
            }
            return this.complete;
        }

        private String formatInitCycleMessage() {
            List<String> attributes = new ArrayList<>();
            if (varsBuildStage == STAGE_INITIALIZING) attributes.add("vars");
            if (completeBuildStage == STAGE_INITIALIZING) attributes.add("complete");
            return "Cannot build CompletionExpectation, attribute initializers form cycle " + attributes;
        }
    }

    /**
     * @return The value of the {@code incompleteAst} attribute
     */
    @Override
    public T getIncompleteAst() {
        return incompleteAst;
    }

    /**
     * @return The value of the {@code expectations} attribute
     */
    @Override
    public ImmutableMap<ITermVar, ITerm> getExpectations() {
        return expectations;
    }

    /**
     * @return The value of the {@code state} attribute
     */
    @Override
    public mb.statix.@Nullable SolverState getState() {
        return state;
    }

    /**
     * @return The computed-at-construction value of the {@code vars} attribute
     */
    @Override
    public Set<ITermVar> getVars() {
        InitShim shim = this.initShim;
        return shim != null
            ? shim.getVars()
            : this.vars;
    }

    /**
     * @return The computed-at-construction value of the {@code complete} attribute
     */
    @Override
    public boolean isComplete() {
        InitShim shim = this.initShim;
        return shim != null
            ? shim.isComplete()
            : this.complete;
    }

    /**
     * Copy the current immutable object by setting a value for the {@link AbstractCompletionExpectation#getIncompleteAst() incompleteAst} attribute.
     * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
     * @param value A new value for incompleteAst
     * @return A modified copy of the {@code this} object
     */
    public final CompletionExpectation<T> withIncompleteAst(T value) {
        if (this.incompleteAst == value) return this;
        T newValue = Objects.requireNonNull(value, "incompleteAst");
        return new CompletionExpectation<>(this, newValue, this.expectations, this.state);
    }

    /**
     * Copy the current immutable object by replacing the {@link AbstractCompletionExpectation#getExpectations() expectations} map with the specified map.
     * Nulls are not permitted as keys or values.
     * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
     * @param entries The entries to be added to the expectations map
     * @return A modified copy of {@code this} object
     */
    public final CompletionExpectation<T> withExpectations(Map<? extends ITermVar, ? extends ITerm> entries) {
        if (this.expectations == entries) return this;
        ImmutableMap<ITermVar, ITerm> newValue = ImmutableMap.copyOf(entries);
        return new CompletionExpectation<>(this, this.incompleteAst, newValue, this.state);
    }

    /**
     * Copy the current immutable object by setting a value for the {@link AbstractCompletionExpectation#getState() state} attribute.
     * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
     * @param value A new value for state (can be {@code null})
     * @return A modified copy of the {@code this} object
     */
    public final CompletionExpectation<T> withState(mb.statix.@Nullable SolverState value) {
        if (this.state == value) return this;
        return new CompletionExpectation<>(this, this.incompleteAst, this.expectations, value);
    }

    /**
     * This instance is equal to all instances of {@code CompletionExpectation} that have equal attribute values.
     * @return {@code true} if {@code this} is equal to {@code another} instance
     */
    @Override
    public boolean equals(Object another) {
        if (this == another) return true;
        return another instanceof CompletionExpectation<?>
            && equalTo((CompletionExpectation<?>) another);
    }

    private boolean equalTo(CompletionExpectation<?> another) {
        return incompleteAst.equals(another.incompleteAst)
            && expectations.equals(another.expectations)
            && Objects.equals(state, another.state)
            && vars.equals(another.vars)
            && complete == another.complete;
    }

    /**
     * Computes a hash code from attributes: {@code incompleteAst}, {@code expectations}, {@code state}, {@code vars}, {@code complete}.
     * @return hashCode value
     */
    @Override
    public int hashCode() {
        int h = 5381;
        h += (h << 5) + incompleteAst.hashCode();
        h += (h << 5) + expectations.hashCode();
        h += (h << 5) + Objects.hashCode(state);
        h += (h << 5) + vars.hashCode();
        h += (h << 5) + Booleans.hashCode(complete);
        return h;
    }

    /**
     * Prints the immutable value {@code CompletionExpectation} with attribute values.
     * @return A string representation of the value
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("CompletionExpectation")
            .omitNullValues()
            .add("incompleteAst", incompleteAst)
            .add("expectations", expectations)
            .add("state", state)
            .add("vars", vars)
            .add("complete", complete)
            .toString();
    }

    /**
     * Construct a new immutable {@code CompletionExpectation} instance.
     * @param <T> generic parameter T
     * @param incompleteAst The value for the {@code incompleteAst} attribute
     * @param expectations The value for the {@code expectations} attribute
     * @param state The value for the {@code state} attribute
     * @return An immutable CompletionExpectation instance
     */
    public static <T extends ITerm> CompletionExpectation<T> of(T incompleteAst, Map<? extends ITermVar, ? extends ITerm> expectations, mb.statix.@Nullable SolverState state) {
        return new CompletionExpectation<>(incompleteAst, expectations, state);
    }

    /**
     * Creates an immutable copy of a {@link AbstractCompletionExpectation} value.
     * Uses accessors to get values to initialize the new immutable instance.
     * If an instance is already immutable, it is returned as is.
     * @param <T> generic parameter T
     * @param instance The instance to copy
     * @return A copied immutable CompletionExpectation instance
     */
    public static <T extends ITerm> CompletionExpectation<T> copyOf(AbstractCompletionExpectation<T> instance) {
        if (instance instanceof CompletionExpectation<?>) {
            return (CompletionExpectation<T>) instance;
        }
        return CompletionExpectation.<T>of(instance.getIncompleteAst(), instance.getExpectations(), instance.getState());
    }
}
