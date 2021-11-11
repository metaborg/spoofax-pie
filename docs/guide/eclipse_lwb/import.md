# Importing a Project

An existing Spoofax 3 language project can be imported into Eclipse as follows.

In the main menu of Eclipse, select <span class="guilabel">File ‣ Import...</span> to open the import dialog.
In the import dialog, select <span class="guilabel">General ‣ Existing Projects into Workspace</span> and press <span class="guilabel">Next ></span> to open the import projects dialog.

In the import projects dialog, ensure <span class="guilabel">Select root directory:</span> is ticked and press <span class="guilabel">Browse...</span> on the right of that.
Select the root directory of the language project that you want to import (the directory that contains `spoofaxc.cfg`) and press <span class="guilabel">Open</span>.
Then, press <span class="guilabel">Finish</span> to import the project.

The project should now be imported into your workspace.
Finally, build the language project by selecting the project in the <span class="guilabel">Package Explorer</span> and choosing <span class="guilabel">Project ‣ Build Project</span>.
Building the project at least once after importing is required to update the project.
