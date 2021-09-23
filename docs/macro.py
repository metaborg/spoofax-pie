from typing import Optional

windows_icon = ":fontawesome-brands-windows:"
macos_icon = ":fontawesome-brands-apple:"
linux_icon = ":fontawesome-brands-linux:"


def download_link(repo: str, artifact: str, classifier: str, packaging: str, version="LATEST", group="org.metaborg"):
    return f"https://artifacts.metaborg.org/service/local/artifact/maven/redirect?r={repo}&g={group}&a={artifact}&c={classifier}&p={packaging}&v={version}"


def eclipse_lwb_download_link(repo: str, variant: str, version: str):
    return download_link(repo, "spoofax.lwb.eclipse.repository", f"spoofax3-{variant}", "zip", version)


def eclipse_lwb_download(icon: str, name: str, repo: str, variant: str, version: str):
    return f"{icon} [{name}]({eclipse_lwb_download_link(repo, variant, version)})"


def fill_env_with_release(env, env_version: str, version: str, download_version: str, date: Optional[str]):
    repo = "snapshots" if "SNAPSHOT" in version else "releases"
    env.variables.release[env_version] = {
        "date": date,
        "version": version,
        "lwb": {"eclipse": {
            "install": {
                "jvm": {
                    "windows": eclipse_lwb_download(windows_icon, "Windows 64-bit with embedded JVM", repo,
                                                    "win32-x86_64-jvm", download_version),
                    "macos": eclipse_lwb_download(macos_icon, "macOS 64-bit with embedded JVM", repo,
                                                  "macosx-x86_64-jvm",
                                                  download_version),
                    "linux": eclipse_lwb_download(linux_icon, "Linux 64-bit with embedded JVM", repo,
                                                  "linux-x86_64-jvm",
                                                  download_version),
                },
                "windows": eclipse_lwb_download(windows_icon, "Windows 64-bit", repo, "win32-x86_64", download_version),
                "macos": eclipse_lwb_download(macos_icon, "macOS 64-bit", repo, "macosx-x86_64", download_version),
                "linux": eclipse_lwb_download(linux_icon, "Linux 64-bit", repo, "linux-x86_64", download_version),
            },
            "repository": f"https://artifacts.metaborg.org/content/unzip/releases-unzipped/org/metaborg/spoofax.lwb.eclipse.repository/{version}/spoofax.lwb.eclipse.repository-{version}.zip-unzip/"
        }}
    }


release_versions = {
    "0.12.0": "22-09-2021",
    "0.11.13": "22-09-2021",
    "0.11.12": "20-09-2021",
    "0.11.11": "17-09-2021",
    "0.11.10": "15-09-2021",
    "0.11.9": "13-09-2021",
    "0.11.8": "13-09-2021",
    "0.11.7": "08-09-2021",
    "0.11.6": "07-09-2021",
    "0.11.5": "06-09-2021",
    "0.11.4": "03-09-2021",
    "0.11.3": "03-09-2021",
    "0.11.2": "03-09-2021",
    "0.11.1": "02-09-2021",
    "0.11.0": "31-08-2021",
    "0.10.0": "25-08-2021",
    "0.9.0": "14-07-2021",
    "0.8.0": "28-05-2021",
}
development_version = "develop-SNAPSHOT"


def define_env(env):
    env.variables.os = {
        "windows": f"{windows_icon} Windows",
        "linux": f"{linux_icon} Linux",
        "macos": f"{macos_icon} macOS",
    }
    env.variables.release = {}
    for version, date in release_versions.items():
        fill_env_with_release(env, version, version, version, date)
    latest_rel_version, latest_rel_date = next(iter(release_versions.items()))
    fill_env_with_release(env, "rel", latest_rel_version, latest_rel_version, latest_rel_date)
    fill_env_with_release(env, "dev", "develop-SNAPSHOT", "LATEST", None)
