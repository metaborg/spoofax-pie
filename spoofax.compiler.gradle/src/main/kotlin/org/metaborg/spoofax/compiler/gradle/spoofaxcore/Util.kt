package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import mb.resource.fs.FSPath
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.Project
import org.gradle.api.provider.Provider

fun Project.toGradleProject(): GradleProject {
  return GradleProject.builder()
    .coordinate(group.toString(), name, version.toString())
    .baseDirectory(FSPath(projectDir))
    .build()
}

inline fun <reified T> Provider<T>.ifPresentDo(crossinline func: (T) -> Unit) {
  if(isPresent) {
    func(get())
  } else {
    null
  }
}

inline fun <reified T, reified R> Provider<T>.ifPresent(crossinline func: (T) -> R): () -> R? {
  return {
    if(isPresent) {
      func(get())
    } else {
      null
    }
  }
}

inline infix fun <reified R> (() -> R?).elseReturn(crossinline func: () -> R): R {
  return this() ?: func()
}
