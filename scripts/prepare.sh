#!/usr/bin/env bash

# use platform version  from the root pom.xml
version=`mvn -N help:evaluate -Dexpression=project.version -q -DforceStdout | grep "^[0-9]"`

# download needed files from vaadin/platform
url=https://raw.githubusercontent.com/vaadin/platform/main/
results=./scripts/generator/results/
src=./scripts/generator/src/
mkdir -p $results
mkdir -p $src
# Take versions.json
curl -L -s "${url}/versions.json" > ${results}/versions.json
perl -pi -e 's/.*{{version}}.*\n//g' ${results}/versions.json
# Take scripts (this allows us not maintain the same scripts twice)
for i in creator replacer transformer writer
do
 [ ! -f "${src}/${i}.js" ] && curl -L -s "${url}/${src}/${i}.js" > ${src}/${i}.js
done

# run the generator
cmd="node scripts/generator/generate.js $version scripts/generator/results/versions.json"
echo Running: "$cmd" >&2
$cmd || exit 1

# copy generated poms to the final place
cp scripts/generator/results/hilla-versions.json packages/java/hilla/hilla-versions.json
cp scripts/generator/results/hilla-react-versions.json packages/java/hilla-react/hilla-react-versions.json

echo "Copied the theme file from flow-components to hilla and hilla-react"
# download the file from flow-components
lumoFile="https://raw.githubusercontent.com/vaadin/flow-components/main/vaadin-lumo-theme-flow-parent/vaadin-lumo-theme-flow/src/main/java/com/vaadin/flow/theme/lumo/Lumo.java"
materialFile="https://raw.githubusercontent.com/vaadin/flow-components/main/vaadin-material-theme-flow-parent/vaadin-material-theme-flow/src/main/java/com/vaadin/flow/theme/material/Material.java"
curl -l -s $lumoFile > ./scripts/generator/results/Lumo.java
curl -l -s $materialFile > ./scripts/generator/results/Material.java

# remove @JsModule and @NpmPackage annotation and their import lines
perl -pi -e 's/.*(JsModule|NpmPackage).*\n//g' ./scripts/generator/results/Lumo.java ./scripts/generator/results/Material.java

# copy the theme files to hilla and hilla-react
mkdir -p ./packages/java/hilla-react/src/main/java/dev/hilla/
mkdir -p ./packages/java/hilla/src/main/java/dev/hilla
cp scripts/generator/results/Lumo.java packages/java/hilla-react/src/main/java/dev/hilla/Lumo.java
cp scripts/generator/results/Lumo.java packages/java/hilla/src/main/java/dev/hilla/Lumo.java
cp scripts/generator/results/Material.java packages/java/hilla-react/src/main/java/dev/hilla/Material.java
cp scripts/generator/results/Material.java packages/java/hilla/src/main/java/dev/hilla/Material.java
