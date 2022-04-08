package mb.spoofax.core.language;

import mb.common.util.ListView;

public interface ResourceExports {
    /**
     * Gets the exports for given (language) identifier.
     *
     * @param id (Language) identifier.
     * @return List of exports.
     * @throws NoResourceExportsException when there are no resource exports for given ID.
     */
    ListView<Export> getExports(String id);
}
