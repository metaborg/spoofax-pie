package mb.spoofax.lwb.eclipse.util;

import mb.spoofax.eclipse.util.BuilderUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CommonBuilder {
    public static final String javaBuilderId = "org.eclipse.jdt.core.javabuilder";
    public static final String mavenBuilderId = "org.eclipse.m2e.core.maven2Builder";
    public static final String gradleBuilderId = "org.eclipse.buildship.core.gradleprojectbuilder";

    public static void appendJavaBuilder(IProject project, @Nullable IProgressMonitor monitor, int... triggers)
        throws CoreException {
        BuilderUtil.append(javaBuilderId, project, monitor, triggers);
    }
}
