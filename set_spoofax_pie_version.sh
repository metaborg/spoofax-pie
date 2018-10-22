#!/usr/bin/env sh

set -eu

OLD_MAVEN_VERSION=$1
NEW_MAVEN_VERSION=$2
OLD_ECLIPSE_VERSION=$3
NEW_ECLIPSE_VERSION=$4

mvn -f pom/pom.xml versions:set -DnewVersion=$NEW_MAVEN_VERSION -DgenerateBackupPoms=false

perl -pi -e "s/$OLD_MAVEN_VERSION/$NEW_MAVEN_VERSION/g" pie.eclipse/pom.xml
perl -pi -e "s/$OLD_ECLIPSE_VERSION/$NEW_ECLIPSE_VERSION/g" pie.eclipse/META-INF/MANIFEST.MF

perl -pi -e "s/$OLD_MAVEN_VERSION/$NEW_MAVEN_VERSION/g" lang/cfg/langspec/metaborg.yaml
perl -pi -e "s/$OLD_MAVEN_VERSION/$NEW_MAVEN_VERSION/g" lang/cfg/example/metaborg.yaml
perl -pi -e "s/$OLD_MAVEN_VERSION/$NEW_MAVEN_VERSION/g" lang/cfg/test/metaborg.yaml
