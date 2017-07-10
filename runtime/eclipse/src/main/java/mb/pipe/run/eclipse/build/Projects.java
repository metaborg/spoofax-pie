package mb.pipe.run.eclipse.build;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;

import mb.pipe.run.eclipse.nature.PipeNature;
import mb.pipe.run.eclipse.util.NatureUtils;

public class Projects {
    private final HashSet<IProject> projects = new HashSet<>();


    public Iterable<IProject> projects() {
        return projects;
    }


    public void addProject(IProject project) {
        projects.add(project);
    }

    public void addProjects(IWorkspaceRoot root) throws CoreException {
        for(IProject project : root.getProjects()) {
            if(NatureUtils.exists(PipeNature.id, project)) {
                addProject(project);
            }
        }
    }

    public void removeProject(IProject project) {
        projects.remove(project);
    }
}
