package mb.statix.multilang.metadata;

import mb.common.result.Result;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import mb.statix.spec.Spec;

public interface SpecManager {
    /**
     * This method loads the spec for a language. While loading, it validates the integrity of the spec.
     *
     * @param languageIds The languages to load the spec for.
     * @return A valid spec for the given languages, or an error when laoding fails, or when the resulting spec is invalid
     */
    Result<Spec, SpecLoadException> getSpecResult(LanguageId... languageIds);
}
