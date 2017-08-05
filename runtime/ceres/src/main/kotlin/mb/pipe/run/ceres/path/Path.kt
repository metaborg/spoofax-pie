package mb.pipe.run.ceres.path

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.ceres.In
import mb.ceres.OutEffectBuilder
import mb.ceres.PathStampers
import mb.pipe.run.core.StaticPipeFacade
import mb.vfs.list.PathMatcher
import mb.vfs.list.PathWalker
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import java.io.IOException
import java.io.Serializable
import java.nio.file.Files
import java.util.stream.Collectors

fun resolve(path: String): PPath {
  return StaticPipeFacade.facade().pathSrv.resolveLocal(path)
}

operator fun PPath.plus(other: PPath): PPath {
  return this.resolve(other)
}

operator fun PPath.plus(other: String): PPath {
  return this.resolve(other)
}


class Exists : Builder<PPath, Boolean> {
  companion object {
    val id = "exists"
  }

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): Boolean {
    require(input, PathStampers.exists)
    return Files.exists(input.javaPath)
  }
}

fun BuildContext.exists(input: PPath) = requireOutput(Exists::class.java, input)


class ListContents @Inject constructor(val pathSrv: PathSrv) : Builder<ListContents.Input, ArrayList<PPath>> {
  companion object {
    val id = "listContents"
  }

  data class Input(val path: PPath, val matcher: PathMatcher?) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): ArrayList<PPath> {
    val (path, matcher) = input
    require(path, PathStampers.modified(matcher))
    if (!Files.isDirectory(path.javaPath)) {
      throw BuildException("Cannot list contents of '$input', it is not a directory")
    }
    try {
      val stream = if (matcher != null) path.list(matcher) else path.list()
      return stream.collect(Collectors.toCollection { ArrayList<PPath>() })
    } catch(e: IOException) {
      throw BuildException("Cannot list contents of '$input'", e)
    }
  }
}

fun BuildContext.listContents(input: ListContents.Input) = requireOutput(ListContents::class.java, input)


class WalkContents @Inject constructor(val pathSrv: PathSrv) : Builder<WalkContents.Input, ArrayList<PPath>> {
  companion object {
    val id = "walkContents"
  }

  data class Input(val path: PPath, val walker: PathWalker?) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): ArrayList<PPath> {
    val (path, walker) = input
    require(path, PathStampers.modified(walker))
    if (!Files.isDirectory(path.javaPath)) {
      throw BuildException("Cannot walk contents of '$input', it is not a directory")
    }
    try {
      val stream = if (walker != null) path.walk(walker) else path.walk()
      return stream.collect(Collectors.toCollection { ArrayList<PPath>() })
    } catch(e: IOException) {
      throw BuildException("Cannot walk contents of '$input'", e)
    }
  }
}

fun BuildContext.walkContents(input: WalkContents.Input) = requireOutput(WalkContents::class.java, input)


class Read : Builder<PPath, String> {
  companion object {
    val id = "read"
  }

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): String {
    require(input, PathStampers.hash)
    try {
      return String(input.readAllBytes())
    } catch(e: IOException) {
      throw BuildException("Reading '$input' failed", e)
    }
  }
}

fun BuildContext.read(input: PPath) = requireOutput(Read::class.java, input)


class Copy : OutEffectBuilder<Copy.Input> {
  companion object {
    val id = "copy"
  }

  data class Input(val from: PPath, val to: PPath) : In

  override val id = Companion.id
  override fun BuildContext.effect(input: Input) {
    val (from, to) = input
    require(from)
    try {
      Files.copy(from.javaPath, to.javaPath)
    } catch(e: IOException) {
      throw BuildException("Copying '${input.from}' to '${input.to}' failed", e)
    }
    generate(to)
  }
}

fun BuildContext.copy(input: Copy.Input) = requireOutput(Copy::class.java, input)
