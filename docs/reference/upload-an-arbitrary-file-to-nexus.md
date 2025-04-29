---
title: "Upload an arbitrary file"
---
# How to upload an arbitrary file to Nexus

1.  Go to [artifacts.metaborg.org](https://artifacts.metaborg.org/) and log in.
2.  Select the `Releases` repository, and click the _Artifact Upload_ tab.
3.  For _GAV Definition_ select _GAV Parameters_.
4.  Disable _Auto Guess_ and enter:
    - Group: `org.eclipse`
    - Artifact: `eclipse-platform`
    - Version: `4.21`
3.  Click _Select Artifacts to Upload_ and select the downloaded `eclipse-platform-4.21-linux-gtk-x86_64.tar.gz`.
4.  Enter:
    - Classifier: `linux-gtk-x86_64` (anything after the version number and following dash until the extension)
    - Extension: `tar.gz`
5.  Click _Add Artifact_.
6.  Click _Upload Artifact(s)_.