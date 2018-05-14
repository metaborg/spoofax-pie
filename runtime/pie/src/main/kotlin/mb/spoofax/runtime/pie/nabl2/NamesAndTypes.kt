package mb.spoofax.runtime.pie.nabl2

import mb.nabl2.solver.ImmutableSolution
import mb.pie.runtime.builtin.util.Tuple5
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.model.style.Styling
import mb.vfs.path.PPath
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
