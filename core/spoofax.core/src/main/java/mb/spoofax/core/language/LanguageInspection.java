package mb.spoofax.core.language;

import mb.common.message.KeyedMessages;
import mb.common.message.Messages;
import mb.common.util.ADT;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

import java.util.function.Function;

@ADT
public abstract class LanguageInspection {
    public static class MultiFileInput {
        public final ResourcePath root;
        public final ResourceWalker walker;
        public final ResourceMatcher matcher;

        public MultiFileInput(ResourcePath root, ResourceWalker walker, ResourceMatcher matcher) {
            this.root = root;
            this.walker = walker;
            this.matcher = matcher;
        }
    }

    interface Cases<R> {
        R multiFile(Function<MultiFileInput, Task<KeyedMessages>> multiFileFunc);

        R singleFile(Function<ResourceKey, Task<Messages>> singleFileFunc);
    }

    public static LanguageInspection multiFile(Function<MultiFileInput, Task<KeyedMessages>> multiFileFunc) {
        return LanguageInspections.multiFile(multiFileFunc);
    }

    public static LanguageInspection singleFile(Function<ResourceKey, Task<Messages>> singleFileFunc) {
        return LanguageInspections.singleFile(singleFileFunc);
    }


    public abstract <R> R match(LanguageInspection.Cases<R> cases);

    public LanguageInspections.CaseOfMatchers.TotalMatcher_MultiFile caseOf() {
        return LanguageInspections.caseOf(this);
    }
}
