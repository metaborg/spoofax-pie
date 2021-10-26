package mb.constraint.pie;

import mb.resource.ResourceKey;

public class MissingResultException extends Exception {
    public final ResourceKey resource;

    public MissingResultException(ResourceKey resource) {
        this.resource = resource;
    }

    @Override public String getMessage() {
        return "Cannot get constraint analysis result for '" + resource + "' as it is missing from the multi-file constraint analysis result";
    }
}
