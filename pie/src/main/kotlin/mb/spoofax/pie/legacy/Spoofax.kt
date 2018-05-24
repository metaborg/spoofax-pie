package mb.spoofax.pie.legacy

import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PPathImpl
import mb.spoofax.legacy.StaticSpoofaxCoreFacade
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import java.net.URI

object Spx {
  fun spoofax() = StaticSpoofaxCoreFacade.spoofax()!!
  fun spoofaxMeta() = StaticSpoofaxCoreFacade.spoofaxMeta()!!
}

val FileObject.pPath: PPath get() = this.name.pPath
val FileName.pPath: PPath get() = PPathImpl(URI(this.uri))
val PPath.fileObject: FileObject get() = Spx.spoofax().resourceService.resolve(this.javaPath.toUri())
