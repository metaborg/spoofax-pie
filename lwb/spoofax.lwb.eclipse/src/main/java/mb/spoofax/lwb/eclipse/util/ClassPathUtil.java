package mb.spoofax.lwb.eclipse.util;

import io.github.classgraph.ClassGraph;
import mb.spoofax.compiler.eclipsebundle.SpoofaxCompilerEclipseBundle;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.lwb.eclipse.SpoofaxLwbPlugin;
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
import java.io.File;
import java.util.List;

public class ClassPathUtil {
    public static List<File> getClassPath() {
        final ClassGraph classGraph = new ClassGraph()
            .addClassLoader(SpoofaxLwbPlugin.class.getClassLoader())
            .addClassLoader(SpoofaxPlugin.class.getClassLoader())
            .addClassLoader(ToolingEclipseBundle.class.getClassLoader())
            .addClassLoader(SpoofaxCompilerEclipseBundle.class.getClassLoader())

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
        return classGraph.getClasspathFiles();
    }
}
