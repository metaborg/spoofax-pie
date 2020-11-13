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

    /**
     * Used in {@link mb.statix.multilang.pie.SmlInstantiateGlobalScope} to create a global scope that is correct in
     * all contexts. That is a hack, because Statix does not support extending scope graphs with new labels.
     * Therefore, this method should be used nowhere else.
     * @return Spec without rules, with all possible labels available.
     */
    Result<Spec, SpecLoadException> getSpecOfAllFragments();
}
