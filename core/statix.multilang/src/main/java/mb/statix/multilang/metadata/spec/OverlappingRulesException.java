package mb.statix.multilang.metadata.spec;

import com.google.common.collect.Multimap;
import mb.statix.spec.Rule;

import java.util.Collection;
import java.util.Map;

public class OverlappingRulesException extends SpecLoadException {
    public OverlappingRulesException(Multimap<String, Rule> rulesWithEquivalentPatterns) {
        super(createMessage(rulesWithEquivalentPatterns), true);
    }

    private static String createMessage(Multimap<String, Rule> rulesWithEquivalentPatterns) {
        StringBuilder messageBuilder = new StringBuilder("BUG: Combined spec has rules with equivalent patterns\n");
        for(Map.Entry<String, Collection<Rule>> entry : rulesWithEquivalentPatterns.asMap().entrySet()) {
            messageBuilder.append(String.format("| Overlapping rules for: %s%n", entry.getKey()));
            for(Rule rule : entry.getValue()) {
                messageBuilder.append(String.format("| * %s%n", rule));
            }
        }

        return messageBuilder.toString();
    }
}
