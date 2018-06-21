package mb.spoofax.pie.legacy

import mb.pie.lang.runtime.util.Tuple2
import mb.pie.vfs.path.PPath
import java.io.Serializable

data class FileTextPair(val file: PPath, val text: String) : Tuple2<PPath, String>, Serializable
