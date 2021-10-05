package mb.codecompletion.bench.utils

import java.nio.file.Path

/**
 * Changes the name of the filename, but keeps the extension unmodified.
 *
 * If the path does not have a filename, it is returned unmodified.
 *
 * @param newName the new name, excluding extension; or an empty string to specify no name
 * @return the new path
 */
fun Path.withName(newName: String): Path = withName { newName }

/**
 * Changes the name of the filename, but keeps the extension unmodified.
 *
 * If the path does not have a filename, it is returned unmodified.
 *
 * @param nameTransformation transforms the existing name (excluding extension, empty if not specified) into a new name
 * @return the new path
 */
fun Path.withName(nameTransformation: (String) -> String): Path {
    val (_, name, extension) = split()
    if (name == null) return this
    return this.resolveSibling(nameTransformation(name) + extension)
}


/**
 * Changes the extension of the filename, but keeps the name unmodified.
 *
 * If the path does not have a filename, it is returned unmodified.
 *
 * @param newExtension the new extension, including a leading dot; or an empty string to specify no extension
 * @return the new path
 */
fun Path.withExtension(newExtension: String): Path = withExtension { newExtension }

/**
 * Changes the extension of the filename, but keeps the name unmodified.
 *
 * If the path does not have a filename, it is returned unmodified.
 *
 * @param extensionTransform transforms the existing extension (with a leading dot, or empty if none specified) into a new extension
 * @return the new path
 */
fun Path.withExtension(extensionTransform: (String) -> String): Path {
    val (_, name, extension) = split()
    if (name == null) return this
    return this.resolveSibling(name + extensionTransform(extension))
}

/**
 * Splits the path into three components: the parent path, the name, and the extension.
 *
 * @return a triple of the parent path, the name, and the extension
 */
fun Path.split(): Triple<Path, String?, String> {
    val filenameString = this.fileName?.toString() ?: return Triple(this, null, "")
    // NOTE: We skip the first character, in case the filename starts with a dot.
    val extensionIndex = filenameString.indexOf('.', 1).takeIf { it >= 0 }
    val name = extensionIndex?.let { filenameString.substring(0, it) } ?: filenameString
    val extension = extensionIndex?.let { filenameString.substring(it) } ?: ""
    return Triple(this.parent, name, extension)
}
