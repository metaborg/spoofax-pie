from typing import Optional

windows_icon = ':fontawesome-brands-windows:'
macos_icon = ':fontawesome-brands-apple:'
linux_icon = ':fontawesome-brands-linux:'

artifacts_url_base = 'https://artifacts.metaborg.org'
artifacts_releases_url = f'{artifacts_url_base}/content/repositories/releases/org/metaborg/'


def artifacts_download(repo, artifact, classifier=None, packaging="jar", version="LATEST", group="org.metaborg"):
    return f'{artifacts_url_base}/service/local/artifact/maven/redirect?r={repo}&g={group}' \
           f'&a={artifact}{f"&c={classifier}" if classifier is not None else ""}&p={packaging}&v={version}'


def eclipse_lwb_artifacts_download(repo: str, variant: str, version: str):
    return artifacts_download(repo, 'spoofax.lwb.eclipse.repository', f'spoofax3-{variant}', "zip", version)


win_jvm_variant = 'win32-x86_64-jvm'
win_variant = 'win32-x86_64'
macos_jvm_variant = 'macosx-x86_64-jvm'
macos_variant = 'macosx-x64'
linux_jvm_variant = 'linux-x86_64-jvm'
linux_variant = 'linux-x86_64'


def download_link(icon: str, name: str, link: str):
    return f'{icon} [{name}]({link})'


def fill_variables_with_release(variables, env_version: str, version: str, download_version: str, date: Optional[str]):
    repo = 'snapshots' if 'SNAPSHOT' in version else 'releases'

    windows = eclipse_lwb_artifacts_download(repo, win_variant, download_version)
    windows_jvm = eclipse_lwb_artifacts_download(repo, win_jvm_variant, download_version)
    macos_jvm = eclipse_lwb_artifacts_download(repo, macos_jvm_variant, download_version)
    macos = eclipse_lwb_artifacts_download(repo, macos_variant, download_version)
    linux_jvm = eclipse_lwb_artifacts_download(repo, linux_jvm_variant, download_version)
    linux = eclipse_lwb_artifacts_download(repo, linux_variant, download_version)
    eclipse_repo = f'https://artifacts.metaborg.org/content/unzip/releases-unzipped/org/metaborg/spoofax.lwb.eclipse' \
                   f'.repository/{version}/spoofax.lwb.eclipse.repository-{version}.zip-unzip/ '

    variables.release[env_version] = dict(
        date=date,
        version=version,
        eclipse_lwb=dict(
            install=dict(
                jvm=dict(
                    link=dict(
                        macos=download_link(macos_icon, "macOS 64-bit with embedded JVM", macos_jvm),
                        linux=download_link(linux_icon, "Linux 64-bit with embedded JVM", linux_jvm),
                        windows=download_link(windows_icon, "Windows 64-bit with embedded JVM", windows_jvm),
                    ),
                    macos=macos_jvm,
                    linux=linux_jvm,
                    windows=windows_jvm,
                ), link=dict(
                    macos=download_link(macos_icon, "macOS 64-bit", macos),
                    linux=download_link(linux_icon, "Linux 64-bit", linux),
                    windows=download_link(windows_icon, "Windows 64-bit", windows),
                ),
                macos=macos,
                linux=linux,
                windows=windows,
            ),
            repository=eclipse_repo,
        ),
    )


release_versions = {
    "0.16.14": "01-12-2021",
    "0.16.13": "25-11-2021",
    "0.16.12": "23-11-2021",
    "0.16.11": "19-11-2021",
    "0.16.10": "19-11-2021",
    "0.16.9": "18-11-2021",
    "0.16.8": "17-11-2021",
    "0.16.7": "17-11-2021",
    "0.16.6": "16-11-2021",
    "0.16.5": "12-11-2021",
    "0.16.4": "11-11-2021",
    "0.16.3": "10-11-2021",
    "0.16.2": "09-11-2021",
    "0.16.1": "08-11-2021",
    "0.16.0": "05-11-2021",
    "0.15.3": "22-10-2021",
    "0.15.2": "21-10-2021",
    "0.15.1": "19-10-2021",
    "0.15.0": "18-10-2021",
    "0.14.2": "13-10-2021",
    "0.14.1": "12-10-2021",
    "0.14.0": "11-10-2021",
    "0.13.0": "01-10-2021",
    "0.12.1": "24-09-2021",
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
    define_macros(env.variables)


def define_macros(variables):
    variables.os = dict(
        windows=f'{windows_icon} Windows',
        linux=f'{linux_icon} Linux',
        macos=f'{macos_icon} macOS'
    )
    variables.release = {}
    for version, date in release_versions.items():
        fill_variables_with_release(variables, version, version, version, date)
    latest_rel_version, latest_rel_date = next(iter(release_versions.items()))
    fill_variables_with_release(variables, 'rel', latest_rel_version, latest_rel_version, latest_rel_date)
    fill_variables_with_release(variables, 'dev', 'develop-SNAPSHOT', 'LATEST', None)
