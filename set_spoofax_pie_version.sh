#!/usr/bin/env sh

set -eu

OLD_VERSION=$1
NEW_VERSION=$2

mvn -f pom/pom.xml versions:set -DnewVersion=$NEW_VERSION -DgenerateBackupPoms=false

perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" lang/cfg/langspec/metaborg.yaml
perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" lang/cfg/example/metaborg.yaml
perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" lang/cfg/test/metaborg.yaml
