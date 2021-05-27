package mb.spoofax.lwb.eclipse.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import java.io.File;
import java.util.ArrayList;

public class JavaProjectUtil {
    public static void configureProject(IProject project, @Nullable IProgressMonitor monitor) throws CoreException {
        // Add source folders to classpath.
        // TODO: sync these hardcoded paths with the compiler
        final IFolder src = getFolder(project, "src", monitor);
        final IFolder srcMain = getFolder(src, "main", monitor);
        final IFolder srcMainJava = getFolder(srcMain, "java", monitor);
        final IFolder build = getDerivedFolder(project, "build", monitor);
        final IFolder buildEclipseClasses = getDerivedFolder(build, "eclipseclasses", monitor);
        final IFolder buildGenerated = getDerivedFolder(build, "generated", monitor);
        final IFolder buildGeneratedSources = getDerivedFolder(buildGenerated, "sources", monitor);
        final IFolder buildGeneratedSourcesLanguage = getDerivedFolder(buildGeneratedSources, "language", monitor);
        final IFolder buildGeneratedSourcesAdapter = getDerivedFolder(buildGeneratedSources, "adapter", monitor);
        final IFolder buildGeneratedSourcesEclipse = getDerivedFolder(buildGeneratedSources, "eclipse", monitor);
        final IFolder buildGeneratedSourcesLanguageSpecification = getDerivedFolder(buildGeneratedSources, "languageSpecification", monitor);
        final IFolder buildGeneratedSourcesLanguageSpecificationJava = getDerivedFolder(buildGeneratedSourcesLanguageSpecification, "java", monitor);
        final IFolder buildGeneratedSourcesAnnotationProcessor = getDerivedFolder(buildGeneratedSources, "annotationProcessor", monitor);
        final IFolder buildGeneratedSourcesAnnotationProcessorJava = getDerivedFolder(buildGeneratedSourcesAnnotationProcessor, "java", monitor);
        final IFolder buildGeneratedSourcesAnnotationProcessorJavaMain = getDerivedFolder(buildGeneratedSourcesAnnotationProcessorJava, "main", monitor);

        final IPath[] emptyPaths = new IPath[]{};
        final IClasspathAttribute optionalAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, "true");
        final IClasspathAttribute[] sourceAttributes = new IClasspathAttribute[]{optionalAttribute};
        final IClasspathAttribute ignoreOptionalProblemsAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, "true");
        final IClasspathAttribute[] derivedSourceAttributes = new IClasspathAttribute[]{optionalAttribute, ignoreOptionalProblemsAttribute};

        final ArrayList<IClasspathEntry> classpathEntries = new ArrayList<>();
        classpathEntries.add(JavaCore.newSourceEntry(srcMainJava.getFullPath(), emptyPaths, emptyPaths, null, sourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesLanguage.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesAdapter.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesEclipse.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesLanguageSpecificationJava.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesAnnotationProcessorJavaMain.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));

        // Add built-in class path entries to classpath.
        for(File classPathEntry : ClassPathUtil.getClassPath()) {
            classpathEntries.add(JavaCore.newLibraryEntry(Path.fromOSString(classPathEntry.getAbsolutePath()), null, null));
        }

        // Add installed JRE to classpath.
        classpathEntries.add(JavaCore.newContainerEntry(Path.fromOSString("org.eclipse.jdt.launching.JRE_CONTAINER")));

        // Set output location and collected classpath entries.
        final IJavaProject javaProject = JavaCore.create(project);
        javaProject.setOutputLocation(buildEclipseClasses.getFullPath(), monitor);
        javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[0]), monitor);

        // Set Java 1.8 compliance.
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        javaProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        javaProject.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
        javaProject.setOption(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
        javaProject.setOption(JavaCore.COMPILER_CODEGEN_INLINE_JSR_BYTECODE, JavaCore.ENABLED);
    }

    private static IFolder getFolder(IContainer parent, String path, @Nullable IProgressMonitor monitor) throws CoreException {
        final IFolder folder = parent.getFolder(Path.fromOSString(path));
        if(!folder.exists()) {
            folder.create(true, true, monitor);
        }
        return folder;
    }

    private static IFolder getDerivedFolder(IContainer parent, String path, @Nullable IProgressMonitor monitor) throws CoreException {
        final IFolder folder = getFolder(parent, path, monitor);
        folder.setDerived(true, monitor);
        return folder;
    }
}
