#!/usr/bin/env sh

set -eu

OLD_VERSION=$1
NEW_VERSION=$2

perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" .mvn/extensions.xml
perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" lang/cfg/langspec/.mvn/extensions.xml
perl -pi -e "s/\<spoofax\.legacy\.version\>$OLD_VERSION\<\/spoofax\.legacy\.version\>/<spoofax.legacy.version>$NEW_VERSION<\/spoofax.legacy.version>/g" pom/pom.xml
