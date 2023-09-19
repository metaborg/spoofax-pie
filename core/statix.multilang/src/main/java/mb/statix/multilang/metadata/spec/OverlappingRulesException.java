package mb.statix.multilang.metadata.spec;

import com.google.common.collect.Multimap;
import mb.statix.spec.Rule;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class OverlappingRulesException extends SpecLoadException {
    public OverlappingRulesException(Map<String, Set<Rule>> rulesWithEquivalentPatterns) {
        super(createMessage(rulesWithEquivalentPatterns), true);
    }

    private static String createMessage(Map<String, Set<Rule>> rulesWithEquivalentPatterns) {
        StringBuilder messageBuilder = new StringBuilder("BUG: Combined spec has rules with equivalent patterns\n");
        for(Map.Entry<String, Set<Rule>> entry : rulesWithEquivalentPatterns.entrySet()) {
            messageBuilder.append(String.format("| Overlapping rules for: %s%n", entry.getKey()));
            for(Rule rule : entry.getValue()) {
                messageBuilder.append(String.format("| * %s%n", rule));
            }
        }

        return messageBuilder.toString();
    }
}
