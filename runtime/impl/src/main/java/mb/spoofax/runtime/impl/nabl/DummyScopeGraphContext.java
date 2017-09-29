package mb.spoofax.runtime.impl.nabl;

import java.util.ArrayList;
import java.util.Collection;

import org.metaborg.meta.nabl2.config.NaBL2Config;
import org.metaborg.meta.nabl2.config.NaBL2DebugConfig;
import org.metaborg.meta.nabl2.spoofax.analysis.IScopeGraphContext;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.concurrent.NullClosableLock;

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
