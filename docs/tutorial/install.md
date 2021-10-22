# Installing the Spoofax 3 language workbench

--8<-- "docs/_include/_all.md"

This tutorial gets you set up for language development in Spoofax 3 by installing the Spoofax 3 Eclipse LWB environment.

## Requirements

Spoofax 3 runs on the major operating systems:

* {{os.windows}} (64 bits)
* {{os.macos}} (64 bits)
* {{os.linux}} (64 bits)

Other than that, nothing is required as everything is contained in the archive we are going to download.

## Download

To get started, we will download a premade Eclipse installation that comes bundled with the Spoofax 3 LWB plugin. We will download version {{release.rel.version}} released on {{release.rel.date}}. Download the latest version for your platform:

* {{release.rel.eclipse_lwb.install.jvm.link.windows}}
* {{release.rel.eclipse_lwb.install.jvm.link.macos}}
* {{release.rel.eclipse_lwb.install.jvm.link.linux}}

These are bundled with an embedded JVM so that you do not need to have a JVM installed. If your system has a JVM of version 11 or higher installed, and would rather use that, use the following download links instead:

* {{release.rel.eclipse_lwb.install.link.windows}}
* {{release.rel.eclipse_lwb.install.link.macos}}
* {{release.rel.eclipse_lwb.install.link.linux}}

## Unpack

Unpack the downloaded archive to a directory with **write access**. Write access is required because Eclipse needs to write to several configuration files inside its installation.

!!! warning
    The unpacked directory or application may be renamed, but do not include spaces or other characters that would not be allowed in a URI (i.e., `: ? # [ ] @`). The same is true for the directory the archive is extracted to. Failing to do so breaks a built-in classpath detection mechanism which will cause Java compilation errors.

!!! warning
    On {{ os.windows }} do not unpack the Eclipse installation into `Program Files`, because no write access is granted there, breaking both Eclipse and Spoofax.

On {{ os.macos }} Sierra (10.12) and above, after unpacking, open the Terminal and navigate to the directory where the `Spoofax3.app` file is located and execute:

```shell
xattr -rc Spoofax3.app
```

On {{ os.linux }}, there is currently a bug where the embedded `java` executable does not have executable permission, leading to a long error when starting Eclipse.
To solve this, run in the eclipse directory:

```shell
chmod +x jvm/bin/java
```

## Running Eclipse

Start up Eclipse, depending on your operating system:

* {{os.windows}}: run `Spoofax3/eclipse.exe`
* {{os.macos}} run `Spoofax3.app`
* {{os.linux}} run `Spoofax3/eclipse`

!!! warning
    {{ os.macos }} Sierra (10.12) and above will mark the unpacked `Spoofax3.app` as "damaged" due to a modified signed/notarized application, because we have modified the eclipse.ini file inside it. To fix this, open the Terminal, navigate to the directory where the `Spoofax3.app` file is located, and execute:

    ```
    xattr -rc Spoofax3.app
    ```

After starting up, choose where your workspace will be stored. The Eclipse workspace will contain all of your settings, and is the default location for new projects.

!!! warning
    Currently, there are several bugs regarding spaces in the workspace path, so ensure there are no spaces on the workspace path.

## Configuring Eclipse's preferences

Some Eclipse preferences unfortunately have sub-optimal defaults. After you have chosen a workspace and Eclipse has completely started up (and you have closed the Welcome page), go to the Eclipse preferences by pressing ++cmd+comma++ on macOS and by going to the <span class="guilabel">Window ‣ Preferences</span> menu on others, and set these options:

* <span class="guilabel">General ‣ Startup and Shutdown</span>
    * Enable: <span class="guilabel">Refresh workspace on startup</span>
* <span class="guilabel">General ‣ Workspace</span>
    * Enable: <span class="guilabel">Refresh using native hooks or polling</span>
* <span class="guilabel">General ‣ Workspace ‣ Build</span>
    * Enable: <span class="guilabel">Save automatically before manual build</span>

We need to make sure that Eclipse has detected an installed JRE. Open the Eclipse preferences and go to the <span class="guilabel">Java ‣ Installed JREs</span> page:

* If there are no installed JREs, and you've downloaded an Eclipse installation *with an embedded JVM*, press <span class="guilabel">Search...</span> and navigate to the location where you unpacked the Eclipse installation, and choose the `jvm` directory in it. Then press the checkmark of the JRE to activate it.
* If there are no installed JREs, and you've downloaded an Eclipse installation *without an embedded JVM*, press <span class="guilabel">Search...</span> and navigate to the location where your JVM installed, and choose it. Then press the checkmark of the JRE to activate it.
* If there are one or more installed JVMs, but none are selected, select an appropriate one by pressing the checkmark.
* If there are one or more installed JVMs, and one is selected, you are good to go.

Finally, you may configure Eclipse to your liking.
Some typical settings to adjust:

* <span class="guilabel">General ‣ Editors ‣ Text Editors</span>
    * <span class="guilabel">Displayed tab width</span>: change to your desired tab width. Most of Spoofax uses 2 by convention.
    * <span class="guilabel">Insert spaces for tabs</span>: enable, as Spoofax uses spaces by convention.
    * <span class="guilabel">Show print margin</span>: enable and set if you want to set a maximum line length.
    * <span class="guilabel">Show line numbers</span>: enable if you want to see line numbers.
* <span class="guilabel">General ‣ Appearance</span>: choose a Theme to your liking.
* <span class="guilabel">General ‣ Appearance ‣ Colors and Fons ‣ Text Font</span>: the JetBrains Mono font supports ligatures and is used in (most of) the lecture slides (install separately).
* <span class="guilabel">General ‣ Keys</span>: change keybindings.


!!! tip
    These preferences are stored per workspace. If you create a fresh workspace, you have to re-do these settings. You can create a new workspace with copied preferences by selecting <span class="guilabel">File ‣ Switch workspace ‣ Other...</span>, and then checking <span class="guilabel">Preferences</span> under <span class="guilabel">Copy settings</span>.

Now that Eclipse is set up, continue with [creating a language project](create_language_project.md)
