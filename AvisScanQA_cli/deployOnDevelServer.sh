#!/usr/bin/env bash

SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")

projectName=$(basename "$SCRIPT_DIR")
develServer=avisscqa@canopus.statsbiblioteket.dk



#Build
(cd "$SCRIPT_DIR/"..; pwd; mvn $1 package -Psbprojects-nexus -DskipTests=true --also-make --projects "$(basename "$SCRIPT_DIR")") || exit 1

version=1.0-SNAPSHOT

echo $version

rsync -av "$SCRIPT_DIR/target/${projectName}-${version}.tar.gz" "${develServer}:."

ssh "${develServer}" "tar -xvzf ./${projectName}-${version}.tar.gz"
