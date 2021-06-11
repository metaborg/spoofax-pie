package mb.spt.lut;

import mb.spt.SptScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@SptScope
public class LanguageUnderTestProviderWrapper {
    private @Nullable LanguageUnderTestProvider provider = null;

    @Inject public LanguageUnderTestProviderWrapper() {}

    public LanguageUnderTestProvider get() {
        if(provider == null) {
            provider = new FailingLanguageUnderTestProvider();
        }
        return provider;
    }

    public void set(LanguageUnderTestProvider provider) {
        if(this.provider != null) {
            throw new IllegalStateException("Provider in LanguageUnderTestProviderWrapper was already set or used. After setting or using the function, it may not be changed any more to guarantee sound incrementality");
        }
        this.provider = provider;
    }
}
