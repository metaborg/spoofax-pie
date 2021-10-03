package mb.codecompletion.bench

import mb.nabl2.terms.ITerm
import mb.nabl2.terms.ITermVar
import mb.nabl2.terms.matching.TermPattern
import mb.nabl2.terms.substitution.ISubstitution
import mb.nabl2.terms.unification.OccursException
import mb.nabl2.terms.unification.ud.IUniDisunifier
import mb.statix.CodeCompletionProposal

/**
 * Replaces the specified term variable with the specified term,
 * if it is the term that we expected.
 *
 * @param `var` the term variable to replace
 * @param proposal the proposal to replace it with, which may contain term variables
 * @return the resulting incomplete AST if replacement succeeded; otherwise, `null` when it doesn't fit
 */
fun tryMatchExpectation(
    placeholder: ITermVar,
    expectedTerm: ITerm,
    actualTerm: ITerm,
//    proposal: CodeCompletionProposal
): Boolean {
//    val actualTerm = proposal.term
//    val newState = proposal.state
//    val expectedTerms = listOf(placeholder to expectedTerm)

    // If trying to replace by the same variable indicates that the proposal
    // did not replace the variable by a term.
    if (placeholder == actualTerm) return false
    // If the variable can never be replaced by the actual term, we reject this proposal.
    val substitution = trySubtitute(actualTerm, expectedTerm) ?: return false
//    // If the expectations cannot unify with the current unifier, we reject this proposal.
//    val expectedUnifier = tryUnifyPairs(proposal.state.state.unifier(), expectedTerms) ?: return false
//    // If the substitution cannot unify with the current unifier, we reject this proposal.
//    val expectedUnifier2 = tryUnify(expectedUnifier, substitution.entrySet()) ?: return false

//    // Additionally, we can only accept a proposal if the other variables can be matched to their new values,
//    // or where the new value is the same as the old value, the new value is a variable, or the new value is unknown.
//    for (v in vars) {
//        if (v == placeholder) continue
//        val actualTerm: ITerm = newState.project(v)
//        val matches = trySubtitute(v, actualTerm) != null
//        if (!matches) {
//            // The variable can never be replaced by the value in the unifier,
//            // so we reject this proposal.
//            return null
//        }
//    }

//    // The substitution shows the new variables and their expected term values
//    val expectedAsts = expectedTerms.toMap().toMutableMap()
//    expectedAsts.remove(placeholder)
//    for ((key, value) in substitution.entrySet()) {
//        expectedAsts.compute(key) { k: ITermVar, v: ITerm? ->
//            check(!(v != null && v != value)) { "Trying to add expectation $k |-> $value, but already has expectation |-> $v." }
//            value
//        }
//        expectedAsts[key] = value
//    }
    return true
//    val newIncompleteAst = PersistentSubstitution.Immutable.of(placeholder, actualTerm).apply(
//        incompleteAst
//    )
//    return CompletionExpectation.of(newIncompleteAst, expectedAsts, newState)
}

fun trySubtitute(expectedTerm: ITerm, actualTerm: ITerm): ISubstitution.Immutable? {
    // Does the term we got, including variables, match the expected term?
    return TermPattern.P().fromTerm(actualTerm).match(expectedTerm).orElse(null)
}

fun tryUnifyPairs(
    unifier: IUniDisunifier.Immutable,
    expectations: Iterable<Pair<ITermVar, ITerm>>
): IUniDisunifier.Immutable? {
    return tryUnify(unifier, expectations.map { it.toEntry() })
}

fun tryUnify(
    unifier: IUniDisunifier.Immutable,
    expectations: Iterable<Map.Entry<ITermVar, ITerm>>
): IUniDisunifier.Immutable? {
    return try {
        unifier.unify(expectations)
            .map { it.unifier() }
            .orElse(null)
    } catch (e: OccursException) {
        null
    }
}

fun <K,V> Pair<K,V>.toEntry() = object: Map.Entry<K,V> {
    override val key: K = first
    override val value: V = second
}
