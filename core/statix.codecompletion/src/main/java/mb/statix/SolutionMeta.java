package mb.statix;

/**
 * Meta data for a solver state.
 */
public final class SolutionMeta {

    private final int expandedQueries;
    private final int expandedRules;

    /**
     * Initializes a new instance of the {@link SolutionMeta} class.
     *
     * @param expandedQueries the number of expanded queries
     * @param expandedRules the number of expanded rules
     */
    public SolutionMeta(int expandedQueries, int expandedRules) {
        this.expandedQueries = expandedQueries;
        this.expandedRules = expandedRules;
    }

    /**
     * Initializes a new instance of the {@link SolutionMeta} class.
     */
    public SolutionMeta() {
        this(0, 0);
    }

    /**
     * The number of expanded queries.
     *
     * @return the number of expanded queries
     */
    public int getExpandedQueries() {
        return expandedQueries;
    }

    /**
     * The number of expanded rules
     *
     * @return the number of expanded rules
     */
    public int getExpandedRules() {
        return expandedRules;
    }

    /**
     * Creates a copy of this {@link SolutionMeta} with the specified number of expanded queries.
     *
     * @param expandedQueries the new number of expanded queries
     * @return the modified copy of the {@link SolutionMeta}
     */
    public SolutionMeta withExpandedQueries(int expandedQueries) {
        return new SolutionMeta(expandedQueries, expandedRules);
    }

    /**
     * Creates a copy of this {@link SolutionMeta} with the specified number of expanded rules.
     *
     * @param expandedRules the new number of expanded rules
     * @return the modified copy of the {@link SolutionMeta}
     */
    public SolutionMeta withExpandedRules(int expandedRules) {
        return new SolutionMeta(expandedQueries, expandedRules);
    }

    /**
     * Creates a copy of this {@link SolutionMeta} with one more expanded query.
     *
     * @return the modified copy of the {@link SolutionMeta}
     */
    public SolutionMeta withExpandedQueriesIncremented() {
        return new SolutionMeta(expandedQueries + 1, expandedRules);
    }

    /**
     * Creates a copy of this {@link SolutionMeta} with one more expanded rule.
     *
     * @return the modified copy of the {@link SolutionMeta}
     */
    public SolutionMeta withExpandedRulesIncremented() {
        return new SolutionMeta(expandedQueries, expandedRules + 1);
    }

    @Override public String toString() {
        return "meta {\n" +
            "expandedQueries=" + expandedQueries + ",\n" +
            "expandedRules=" + expandedRules + "\n" +
            "}";
    }
}
