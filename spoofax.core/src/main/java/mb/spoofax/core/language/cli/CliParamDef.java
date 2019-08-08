package mb.spoofax.core.language.cli;

import mb.common.util.MapView;

import java.util.HashMap;

public class CliParamDef {
    public final MapView<String, CliParam> params;

    public CliParamDef(MapView<String, CliParam> params) {
        this.params = params;
    }

    public CliParamDef(Iterable<CliParam> params) {
        final HashMap<String, CliParam> paramsMap = new HashMap<>();
        for(CliParam param : params) {
            paramsMap.put(param.getParamId(), param);
        }
        this.params = new MapView<>(paramsMap);
    }

    public CliParamDef(CliParam... params) {
        final HashMap<String, CliParam> paramsMap = new HashMap<>();
        for(CliParam param : params) {
            paramsMap.put(param.getParamId(), param);
        }
        this.params = new MapView<>(paramsMap);
    }

    public CliParamDef(CliParam param) {
        final HashMap<String, CliParam> paramsMap = new HashMap<>();
        paramsMap.put(param.getParamId(), param);
        this.params = new MapView<>(paramsMap);
    }

    public CliParamDef() {
        this.params = MapView.of();
    }
}
