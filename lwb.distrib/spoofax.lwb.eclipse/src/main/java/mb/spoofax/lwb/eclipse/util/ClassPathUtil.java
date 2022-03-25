package mb.spoofax.lwb.eclipse.util;

import io.github.classgraph.ClassGraph;
import mb.common.util.ListView;
import mb.dynamix_runtime.eclipse.DynamixRuntimeEclipseComponent;
import mb.gpp.eclipse.GppEclipseParticipant;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.rv32im.eclipse.Rv32ImEclipseComponent;
import mb.spoofax.compiler.eclipsebundle.SpoofaxCompilerEclipseBundle;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.lwb.eclipse.SpoofaxLwbPlugin;
import mb.strategolib.eclipse.StrategoLibEclipseParticipant;
import mb.tooling.eclipsebundle.ToolingEclipseBundle;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.BundleActivator;

import javax.annotation.Generated;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Comparator;
import java.util.List;

public class ClassPathUtil {
    private static @Nullable List<File> classPath;

    public static List<File> getClassPath() {
        if(classPath != null) {
            return classPath;
        }
        final ClassGraph classGraph = new ClassGraph()
            .addClassLoader(SpoofaxLwbPlugin.class.getClassLoader())
            .addClassLoader(SpoofaxPlugin.class.getClassLoader())
            .addClassLoader(ToolingEclipseBundle.class.getClassLoader())
            .addClassLoader(SpoofaxCompilerEclipseBundle.class.getClassLoader())
            .addClassLoader(StrategoLibEclipseParticipant.class.getClassLoader())
            .addClassLoader(GppEclipseParticipant.class.getClassLoader())
            .addClassLoader(Rv32ImEclipseComponent.class.getClassLoader())
            .addClassLoader(DynamixRuntimeEclipseComponent.class.getClassLoader())

            .addClassLoader(Generated.class.getClassLoader()) // Artifact: javax.annotation:jsr250-api:1.0

            .addClassLoader(BundleActivator.class.getClassLoader()) // Bundle: org.eclipse.osgi
            .addClassLoader(IConfigurationElement.class.getClassLoader()) // Bundle: org.eclipse.equinox.registry
            .addClassLoader(CoreException.class.getClassLoader()) // Bundle: org.eclipse.equinox.common
            .addClassLoader(Platform.class.getClassLoader()) // Bundle: org.eclipse.core.runtime
            .addClassLoader(IProjectNature.class.getClassLoader()) // Bundle: org.eclipse.core.resources
            .addClassLoader(Job.class.getClassLoader()) // Bundle: org.eclipse.core.jobs
            .addClassLoader(IContentType.class.getClassLoader()) // Bundle: org.eclipse.core.contenttype
            .addClassLoader(AbstractHandler.class.getClassLoader()) // Bundle: org.eclipse.core.commands
            .addClassLoader(IWindowListener.class.getClassLoader()) // Bundle: org.eclipse.ui.workbench
            .addClassLoader(IDocumentProvider.class.getClassLoader()) // Bundle org.eclipse.ui.workbench.texteditor
            .addClassLoader(TextFileDocumentProvider.class.getClassLoader()) // Bundle: org.eclipse.ui.editors
            .addClassLoader(ContributionItem.class.getClassLoader()) // Bundle: org.eclipse.jface
            .addClassLoader(ISourceViewer.class.getClassLoader()) // Bundle: org.eclipse.jface.text
            .addClassLoader(IAnnotationModel.class.getClassLoader()) // Bundle: org.eclipse.text
            .addClassLoader(Composite.class.getClassLoader()) // Bundle: org.eclipse.swt.*
            ;
        classPath = classGraph.getClasspathFiles();
        classPath.sort(Comparator.naturalOrder());
        return classPath;
    }

    public static class ClassPathSupplier implements Supplier<ListView<File>> {
        private static final ClassPathSupplier instance = new ClassPathSupplier();

        private ClassPathSupplier() {}

        @Override public ListView<File> get(ExecContext context) {
            return ListView.of(getClassPath());
        }

        @Override public boolean equals(@org.checkerframework.checker.nullness.qual.Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() {return 0;}

        @Override public String toString() {return getClass().getSimpleName();}

        private Object readResolve() {
            return instance;
        }
    }

    public static Supplier<ListView<File>> getClassPathSupplier() {
        return ClassPathSupplier.instance;
    }
}
