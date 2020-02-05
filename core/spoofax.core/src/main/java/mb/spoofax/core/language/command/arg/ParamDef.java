package mb.spoofax.core.language.command.arg;

import mb.common.util.MapView;

import java.util.HashMap;

public class ParamDef {
    public final MapView<String, Param> params;

    public ParamDef(MapView<String, Param> params) {
        this.params = params;
    }

    public ParamDef(Iterable<Param> params) {
        final HashMap<String, Param> paramsMap = new HashMap<>();
        for(Param param : params) {
            paramsMap.put(param.getId(), param);
        }
        this.params = new MapView<>(paramsMap);
    }

    public ParamDef(Param... params) {
        final HashMap<String, Param> paramsMap = new HashMap<>();
        for(Param param : params) {
            paramsMap.put(param.getId(), param);
        }
        this.params = new MapView<>(paramsMap);
    }

    public ParamDef(Param param) {
        final HashMap<String, Param> paramsMap = new HashMap<>();
        paramsMap.put(param.getId(), param);
        this.params = new MapView<>(paramsMap);
    }

    public ParamDef() {
        this.params = MapView.of();
    }
}
