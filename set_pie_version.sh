#!/usr/bin/env sh

set -eu

OLD_VERSION=$1
NEW_VERSION=$2

perl -pi -e "s/\<pie\.version\>$OLD_VERSION\<\/pie\.version\>/<pie.version>$NEW_VERSION<\/pie.version>/g" pom/pom.xml
