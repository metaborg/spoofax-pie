package mb.spt.expectation;

import mb.pie.api.ExecContext;
import mb.spoofax.core.CoordinateRequirement;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.TestCase;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ExpectationFragmentUtil {
    @SuppressWarnings("ConstantConditions")
    public static @Nullable LanguageUnderTest getLanguageUnderTest(
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        @Nullable CoordinateRequirement languageCoordinateRequirementHint
    ) {
        final @Nullable LanguageUnderTest fragmentLanguageUnderTest;
        if(languageCoordinateRequirementHint == null) {
            fragmentLanguageUnderTest = languageUnderTest;
        } else {
            fragmentLanguageUnderTest = languageUnderTestProvider.provide(context, testCase.testSuiteFile, testCase.rootDirectoryHint, languageCoordinateRequirementHint)
                .mapOrElse(lut -> lut, e -> null);
        }
        return fragmentLanguageUnderTest;
    }
}
