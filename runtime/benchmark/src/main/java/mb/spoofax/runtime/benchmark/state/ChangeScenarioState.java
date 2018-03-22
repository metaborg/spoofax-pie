package mb.spoofax.runtime.benchmark.state;

import mb.spoofax.runtime.benchmark.state.exec.BUTopsortState;
import mb.spoofax.runtime.benchmark.state.exec.TDState;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;


@State(Scope.Benchmark)
public class ChangeScenarioState {
    public enum Change {
        no_change {
            @Override public void apply(TDState state) {

            }

            @Override public void apply(BUTopsortState state) {

            }
        },
        change_editor_text {
            @Override public void apply(TDState state) {

            }

            @Override public void apply(BUTopsortState state) {

            }
        },
        open_new_editor {
            @Override public void apply(TDState state) {

            }

            @Override public void apply(BUTopsortState state) {

            }
        },
        change_syntax_styling_spec {
            @Override public void apply(TDState state) {

            }

            @Override public void apply(BUTopsortState state) {

            }
        },
        change_syntax_spec {
            @Override public void apply(TDState state) {

            }

            @Override public void apply(BUTopsortState state) {

            }
        },
        change_name_binding_spec {
            @Override public void apply(TDState state) {

            }

            @Override public void apply(BUTopsortState state) {

            }
        },
        change_language_spec_config {
            @Override public void apply(TDState state) {

            }

            @Override public void apply(BUTopsortState state) {

            }
        },
        change_workspace_config {
            @Override public void apply(TDState state) {

            }

            @Override public void apply(BUTopsortState state) {

            }
        };

        public abstract void apply(TDState state);

        public abstract void apply(BUTopsortState state);
    }
}
