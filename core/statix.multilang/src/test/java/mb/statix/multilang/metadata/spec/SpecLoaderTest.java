package mb.statix.multilang.metadata.spec;

import mb.resource.DefaultResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.statix.spec.RuleSet;
import mb.statix.spec.Spec;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SpecLoaderTest {

    private final ClassLoaderResourceRegistry resourceRegistry = new ClassLoaderResourceRegistry(SpecLoaderTest.class.getClassLoader());
    private final ITermFactory termFactory = new TermFactory();

    @Test void loadEmptySpec() throws SpecLoadException {
        final ClassLoaderResource statixRoot = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/empty"));

        Spec spec = SpecUtils
            .loadSpec(statixRoot, "empty", termFactory)
            .toSpec();
        assertNotNull(spec);
    }

    @Test void loadRegularSpec() throws SpecLoadException {
        final ClassLoaderResource statixRoot = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/base"));
        Spec spec = SpecUtils
            .loadSpec(statixRoot, "base", termFactory)
            .toSpec();
        assertNotNull(spec);

        assertEquals(0, spec.edgeLabels().size());
        assertEquals(1, spec.relationLabels().size()); // Decl()
        RuleSet rules = spec.rules();
        assertEquals(1, rules.getRules("base!ok").size());
        assertEquals(1, rules.getRules("imported!rule").size());
    }

    @Test void loadCompatibleSpecs() throws SpecLoadException {
        final ClassLoaderResource root1 = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/base"));
        SpecFragment specFragment1 = SpecUtils
            .loadSpec(root1, "base", termFactory);

        final ClassLoaderResource root2 = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/compatible"));
        SpecFragment specFragment2 = SpecUtils
            .loadSpec(root2, "compatible", termFactory);

        Spec spec = SpecUtils.mergeSpecs(specFragment1.toSpec(), specFragment2.toSpec()).unwrap();

        assertEquals(0, spec.edgeLabels().size());
        assertEquals(1, spec.relationLabels().size()); // Decl()
        RuleSet rules = spec.rules();
        assertEquals(1, rules.getRules("root!ok").size());
        assertEquals(2, rules.getRules("imported!rule").size());
    }

    @Test void loadIncompatibleSpecs() throws SpecLoadException {
        final ClassLoaderResource root1 = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/base"));
        SpecFragment specFragment1 = SpecUtils
            .loadSpec(root1, "base", termFactory);

        final ClassLoaderResource root2 = resourceRegistry
            .getResource(new DefaultResourceKeyString("mb/statix/multilang/incompatible"));
        SpecFragment specFragment2 = SpecUtils
            .loadSpec(root2, "incompatible", termFactory);

        Spec combinedSpec = SpecUtils.mergeSpecs(specFragment1.toSpec(), specFragment2.toSpec()).unwrap();

        assertFalse(combinedSpec.rules().getAllEquivalentRules().isEmpty());
        assertEquals(2, combinedSpec.rules().getRules("imported!rule").size());
    }
}
