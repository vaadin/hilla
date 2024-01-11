#!/bin/bash

# Run this script from your project directory to link all Hilla npm packages to this checkout of Hilla
# e.g.
# cd /tmp
# npx @hilla/cli init  myproj
# cd myproj
# ~/projects/hilla/scripts/link-npm.sh

hillaDir=`dirname $0`/..

pkgs=""
for tsPkg in $hillaDir/packages/ts/*
do
  pkgs+=" "@vaadin/hilla-`basename $tsPkg`@$tsPkg
done

npm i $pkgs --offline
