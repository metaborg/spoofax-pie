package mb.codecompletion.bench.utils

import mb.jsglr.pie.JsglrParseTaskDef
import mb.jsglr.pie.JsglrParseTaskInput
import mb.pie.api.ExecContext
import mb.resource.ResourceKey
import org.spoofax.interpreter.terms.IStrategoTerm
import kotlin.streams.asSequence

/**
 * Parses the resource with the specified key into an ATerm.
 *
 * @param ctx the execution context
 * @param resourceKey the resource key
 * @return the ATerm
 */
fun JsglrParseTaskDef.runParse(ctx: ExecContext, resourceKey: ResourceKey): IStrategoTerm {
    val jsglrResult = ctx.require(this, JsglrParseTaskInput.builder()
        .withFile(resourceKey)
        .build()
    ).unwrap()
    check(!jsglrResult.ambiguous) { "${resourceKey}: Parse result is ambiguous."}
    check(!jsglrResult.messages.containsErrorOrHigher()) { "${resourceKey}: Parse result has errors: ${jsglrResult.messages.stream().asSequence().joinToString { "${it.region}: ${it.text}" }}"}
    return jsglrResult.ast
}
