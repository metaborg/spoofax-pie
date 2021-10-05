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
): Boolean {
    // If trying to replace by the same variable indicates that the proposal
    // did not replace the variable by a term.
    if (placeholder == actualTerm) return false
    // If the variable can never be replaced by the actual term, we reject this proposal.
    return trySubtitute(expectedTerm, actualTerm) != null
}

fun trySubtitute(expectedTerm: ITerm, actualTerm: ITerm): ISubstitution.Immutable? {
    // Does the term we got, including variables, match the expected term?
    return TermPattern.P().fromTerm(actualTerm).match(expectedTerm).orElse(null)
}

