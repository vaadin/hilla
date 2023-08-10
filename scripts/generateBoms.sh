#!/usr/bin/env bash

# use platform version  from the root pom.xml
version=`mvn -N help:evaluate -Dexpression=project.version -q -DforceStdout | grep "^[0-9]"`

snapshot=$1

# install npm deps needed for the generator node script
[ ! -d scripts/generator/node_modules ] && (cd scripts/generator && npm install)

# run the generator
cmd="node scripts/generator/generate.js --platform=$version --versions=versions.json $snapshot"
echo Running: "$cmd" >&2
$cmd || exit 1

# copy generated poms to the final place

cp scripts/generator/results/hilla-versions.json packages/java/hilla/hilla-versions.json
cp scripts/generator/results/hilla-react-versions.json packages/java/hilla-react/hilla-react-versions.json
