package mb.spoofax.runtime.pie.builder.core

import mb.spoofax.runtime.impl.legacy.StaticSpoofaxCoreFacade
import mb.vfs.path.PPath
import mb.vfs.path.PPathImpl
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
