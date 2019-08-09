package mb.spoofax.core.language.cli;

import mb.common.util.ListView;

public class CliParamDef {
    public final ListView<CliParam> params;

    public CliParamDef(ListView<CliParam> params) {
        this.params = params;
    }

    public CliParamDef(Iterable<CliParam> params) {
        this.params = ListView.of(params);
    }

    public CliParamDef(CliParam... params) {
        this.params = ListView.of(params);
    }

    public CliParamDef(CliParam param) {
        this.params = ListView.of(param);
    }

    public CliParamDef() {
        this.params = ListView.of();
    }
}
