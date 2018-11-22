package mb.spoofax.pie.legacy

import mb.fs.api.node.FSNode
import mb.fs.api.path.FSPath
import mb.fs.java.JavaFSNode
import mb.fs.java.JavaFSPath
import mb.spoofax.legacy.StaticSpoofaxCoreFacade
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import java.net.URI

object Spx {
  fun spoofax() = StaticSpoofaxCoreFacade.spoofax()!!
  fun spoofaxMeta() = StaticSpoofaxCoreFacade.spoofaxMeta()!!
}

val FileObject.fsPath: JavaFSPath get() = this.name.fsPath
val FileName.fsPath: JavaFSPath get() = JavaFSPath(URI(this.uri))
val FSPath.fileObject: FileObject get() = Spx.spoofax().resourceService.resolve(this.toString())

val FileObject.fsNode: JavaFSNode get() = this.name.fsNode
val FileName.fsNode: JavaFSNode get() = JavaFSNode(URI(this.uri))
val FSNode.fileObject: FileObject get() = Spx.spoofax().resourceService.resolve(this.path.toString())