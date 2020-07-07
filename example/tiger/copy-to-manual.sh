#!/usr/bin/env bash
set -o pipefail
set -o errexit
set -o errtrace
set -o nounset
#set -o xtrace

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

FROMDIR="${DIR}/generated"
TODIR="${DIR}/manual"
APPNAME="tiger"
SUFFIXES=("" ".cli" ".eclipse" ".eclipse.externaldeps" ".intellij" ".spoofax")

for s in "${SUFFIXES[@]}"; do
    echo "Copying from ${FROMDIR}/${APPNAME}${s}/ to ${TODIR}/${APPNAME}${s}/"
    find "${FROMDIR}"/"${APPNAME}""${s}" -maxdepth 1 -type f | xargs -I {} cp {} "${TODIR}"/"${APPNAME}""${s}"/ || true
    cp -R "${FROMDIR}"/"${APPNAME}""${s}"/src/ "${TODIR}"/"${APPNAME}"/src/ || true
    cp -R "${FROMDIR}"/"${APPNAME}""${s}"/build/generated/sources/spoofax/ "${TODIR}"/"${APPNAME}""${s}"/src/main/ || true
done
