# Spoofax 3 Documentation

## Local build

### With Python

First install dependencies by running in the root of this repository:

```shell
python3 -m pip install --requirement mkdocs_requirements.txt
```

To auto-build the documentation, run:

```shell
mkdocs serve
```

### With docker

To auto-build the documentation, run:

```bash
make
```

## Deploying

To deploy the documentation, just push changes to GitHub.
The [Build and publish documentation workflow](https://github.com/metaborg/spoofax-pie/actions/workflows/documentation.yml) will then build and deploy the documentation.
The documentation for `branch` will be available at `https://www.spoofax.dev/spoofax-pie/branch/`, for example [https://www.spoofax.dev/spoofax-pie/develop/](https://www.spoofax.dev/spoofax-pie/develop/).
