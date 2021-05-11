# Documentation

In this section, we explain the documentation technology and how it is structured.

## Technology

This documentation is developed with [MkDocs](https://www.mkdocs.org/), a fast and simple static site generated that's geared towards building project documentation from Markdown files. MkDocs uses [Python-Markdown](https://python-markdown.github.io/) to process Markdown files, along with [PyMdown Extensions](https://facelessuser.github.io/pymdown-extensions/). We use the [Material for MkDocs theme](https://squidfunk.github.io/mkdocs-material/) which provides a clean look, easy customization, and many features for technical documentation.

We use the following MkDocs plugins:

* [mkdocs-macros-plugin](https://mkdocs-macros-plugin.readthedocs.io/en/latest/) to enable the use of variables, macros, and filters in Markdown files.
* [mkdocs-git-revision-date-plugin](https://github.com/zhaoterryy/mkdocs-git-revision-date-plugin) to add a changed date to the footer based on the last time the file was changed in the Git repository.

The documentation is automatically built and published on a commit to the master branch of this repository using the GitHub actions workflow at `.github/workflows/documentation.yml`.

## Structure

The structure of this documentation follows [The documentation system](https://documentation.divio.com/) where documentation is split into four categories:

* **Tutorials**: oriented to *learning*, *enabling newcomers to get started* through a *lesson*, analogous to *teaching a child how to cook*.
* **How-to guides**: oriented to a *particular goal*, *showing how to solve a specific problem* through a *series of steps*, analogous to a *recipe in a cookery book*.
* **Reference**: oriented to *information*, *describing the machinery* through *dry description*, analogous to an *encyclopaedia article*.
* **Explanation**: oriented to *understanding*, *explaining* through *discursive explanation*, analogous to an *article on culinary social history*.
