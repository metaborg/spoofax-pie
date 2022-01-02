package mb.spoofax.lwb.eclipse.util;

import mb.spoofax.eclipse.util.NatureUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CommonNature {
    public static final String javaNatureId = "org.eclipse.jdt.core.javanature";
    public static final String mavenNatureId = "org.eclipse.m2e.core.maven2Nature";
    public static final String gradleNatureId = "org.eclipse.buildship.core.gradleprojectnature";

    public static void addJavaNature(IProject project, @Nullable IProgressMonitor monitor) throws CoreException {
        NatureUtil.addTo(javaNatureId, project, monitor);
    }
}
