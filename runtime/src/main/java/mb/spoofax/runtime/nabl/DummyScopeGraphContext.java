package mb.spoofax.runtime.nabl;

import mb.nabl2.config.NaBL2Config;
import mb.nabl2.config.NaBL2DebugConfig;
import mb.nabl2.spoofax.analysis.IScopeGraphContext;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.concurrent.NullClosableLock;

import java.util.ArrayList;
import java.util.Collection;

public class DummyScopeGraphContext implements IScopeGraphContext<DummyScopeGraphUnit> {
    @Override public IClosableLock guard() {
        return new NullClosableLock();
    }

    @Override public DummyScopeGraphUnit unit(String resource) {
        return new DummyScopeGraphUnit();
    }

    @Override public Collection<DummyScopeGraphUnit> units() {
        return new ArrayList<>();
    }

    @Override public NaBL2Config config() {
        return new NaBL2Config(false, NaBL2DebugConfig.NONE);
    }
}
