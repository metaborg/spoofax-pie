package mb.statix.multilang;

import mb.resource.ResourceKey;

public class FileAnalysisException extends MultiLangAnalysisException {

    public FileAnalysisException(ResourceKey resourceKey, Throwable throwable) {
        super(resourceKey, resourceKey.toString(), throwable);
    }
}
