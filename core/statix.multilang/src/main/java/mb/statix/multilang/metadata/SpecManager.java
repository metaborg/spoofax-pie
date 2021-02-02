package mb.statix.multilang.metadata;

import mb.common.result.Result;
import mb.statix.multilang.metadata.spec.SpecConfig;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import mb.statix.spec.Spec;

public interface SpecManager {
    Result<SpecConfig, SpecLoadException> getSpecConfig(SpecFragmentId id);
}
