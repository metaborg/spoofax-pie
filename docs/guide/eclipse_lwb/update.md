# Updating & Downgrading

## Updating

The Spoofax 3 language workbench Eclipse plugin can be updated as follows.
In the main menu of Eclipse, select <span class="guilabel">Help ‣ Install New Software...</span> to open the install dialog.
In this dialog, copy the update site of the version you want to install into the <span class="guilabel">Work with:</span> field and press ++enter++.
If you want to install the latest released version, use this update site:

```
{{release.rel.eclipse_lwb.repository}}
```

In the table below, check the checkbox next to <span class="guilabel">Spoofax LWB</span> and press <span class="guilabel">Next ></span>.
A dialog will pop up with install details.
Press <span class="guilabel">Finish</span> and wait for Eclipse to install the plugin.

If a <span class="guilabel">Security Warning</span> pops up, press <span class="guilabel">Install anyway</span>, as Spoofax 3 is currently not signed.

Finally, a dialog will pop up asking you to restart Eclipse.
Press <span class="guilabel">Restart Now</span> to restart Eclipse.

After restarting Eclipse, all projects must be cleaned in order to prevent compatibility issues.
In the main menu of Eclipse, select <span class="guilabel">Project ‣ Clean...</span>, check the <span class="guilabel">Clean all project</span> checkbox, and press <span class="guilabel">Clean</span>.
Then, rebuild all projects with <span class="guilabel">Project ‣ Build All</span> from the main menu, or by pressing ++cmd+b++ on macOS or ++ctrl+b++ on others.

## Downgrading

Eclipse does not support directly downgrading to a previous version.
Therefore, we must first uninstall the plugin.

In the main menu, select <span class="guilabel">Help ‣ Install New Software...</span> to open a new dialog.
In this dialog, click the <span class="guilabel">already installed</span> link to open a list of all installed features.
In that list, select <span class="guilabel">Spoofax LWB</span> and press <span class="guilabel">Uninstall...</span>.

A dialog will pop up detailing the uninstall details.
Press <span class="guilabel">Finish</span> and wait for Eclipse to uninstall the plugin.
Finally, a dialog will pop up asking you to restart Eclipse.
Press <span class="guilabel">Restart Now</span> to restart Eclipse.

After Eclipse has restarted, follow the updating instructions above, but instead of using the latest update site, use the update site of an older version instead.
