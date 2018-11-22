package mb.spoofax.pie.legacy

import mb.fs.java.JavaFSPath
import mb.pie.lang.runtime.Tuple2
import java.io.Serializable

data class FileTextPair(val file: JavaFSPath, val text: String) : Tuple2<JavaFSPath, String>, Serializable
