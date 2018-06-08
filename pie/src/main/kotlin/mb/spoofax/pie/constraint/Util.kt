package mb.spoofax.pie.constraint

import mb.nabl2.solver.ImmutableSolution
import mb.pie.lang.runtime.util.Tuple5
import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Msg
import mb.spoofax.api.parse.Token
import mb.spoofax.api.style.Styling
import java.util.*

fun filterNullPartialSolutions(partialSolutions: ArrayList<ImmutableSolution?>): ArrayList<ImmutableSolution> {
  return partialSolutions.filterNotNull().toCollection(ArrayList())
}

fun extractPartialSolution(result: Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ImmutableSolution?>): ImmutableSolution? {
  return result.component5()
}

fun extractOrRemovePartialSolution(fileToIgnore: PPath, result: Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ImmutableSolution?>): ImmutableSolution? {
  val (file, _, _, _, partialSolution) = result
  return if(file == fileToIgnore) null else partialSolution
}
