package mb.pipe.run.ceres.spoofax.core

import mb.ceres.CPath
import mb.pipe.run.core.path.PPath
import mb.pipe.run.core.path.PPathImpl
import mb.pipe.run.spoofax.util.StaticSpoofax
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import java.net.URI

object Spx {
  fun spoofax() = StaticSpoofax.spoofax()!!
  fun spoofaxMeta() = StaticSpoofax.spoofaxMeta()!!
}


val FileObject.pPath: PPath get() = this.name.pPath
val FileName.pPath: PPath get() = PPathImpl(URI(this.uri))
val PPath.fileObject: FileObject get() = Spx.spoofax().resourceService.resolve(this.javaPath.toUri())

val FileObject.cPath: CPath get() = this.name.cPath
val FileName.cPath: CPath get() = CPath(URI(this.uri))
val CPath.fileObject: FileObject get() = Spx.spoofax().resourceService.resolve(this.javaPath.toUri())

