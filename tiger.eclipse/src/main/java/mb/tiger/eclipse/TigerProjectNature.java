package mb.tiger.eclipse;

import mb.spoofax.eclipse.nature.SpoofaxProjectNature;
import mb.spoofax.eclipse.util.BuilderUtils;
import org.eclipse.core.runtime.CoreException;

public class TigerProjectNature extends SpoofaxProjectNature {
    public static final String id = TigerPlugin.pluginId + ".nature";

    @Override public void configure() throws CoreException {
        BuilderUtils.append(TigerProjectBuilder.id, getProject(), null);
    }

    @Override public void deconfigure() throws CoreException {
        BuilderUtils.removeFrom(TigerProjectBuilder.id, getProject(), null);
    }
}
