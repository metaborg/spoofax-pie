package mb.cfg;

import mb.common.util.SetView;

import java.io.Serializable;

public enum DependencyKind implements Serializable {
    Build, Run;

    public static final SetView<DependencyKind> all = SetView.of(Build, Run);
}
