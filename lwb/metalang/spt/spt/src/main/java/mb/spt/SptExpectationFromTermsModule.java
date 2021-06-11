package mb.spt;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.common.util.MapView;
import mb.spt.expectation.ParseExpectationFromTerm;
import mb.spt.fromterm.TestExpectationFromTerm;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.terms.TermFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Module
public class SptExpectationFromTermsModule {
    @Provides @ElementsIntoSet
    static Set<TestExpectationFromTerm> provideTestExpectationFromTermsIntoSet() {
        final HashSet<TestExpectationFromTerm> testExpectations = new HashSet<>();
        testExpectations.add(new ParseExpectationFromTerm());
        return testExpectations;
    }


    @Provides
    static Map<IStrategoConstructor, TestExpectationFromTerm> provideTestExpectationFromTermsIntoMap(Set<TestExpectationFromTerm> set) {
        final TermFactory termFactory = new TermFactory();
        final HashMap<IStrategoConstructor, TestExpectationFromTerm> map = new HashMap<>(set.size());
        for(TestExpectationFromTerm testExpectationFromTerm : set) {
            for(IStrategoConstructor constructor : testExpectationFromTerm.getMatchingConstructors(termFactory)) {
                final @Nullable TestExpectationFromTerm existing = map.put(constructor, testExpectationFromTerm);
                if(existing != null) {
                    throw new IllegalArgumentException("TestExpectationFromTerm entry for constructor '" + constructor + "' already exists: " + existing);
                }
            }
        }
        return map;
    }

    @Provides
    static MapView<IStrategoConstructor, TestExpectationFromTerm> provideTestExpectationFromTermsIntoMapView(Map<IStrategoConstructor, TestExpectationFromTerm> map) {
        return MapView.of(map);
    }
}
