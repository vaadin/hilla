#!/usr/bin/env bash

# Fails the script if any command failed or any variable is unset
set -eu

branch=main

# shellcheck disable=SC2139
alias ghr="curl https://api.github.com/repos/$REPO/branches/$branch/protection \
  -H 'Accept: application/vnd.github.v3+json' \
  -H 'Authorization: token $GITHUB_RELEASE_TOKEN' \
  -s"

node scripts/update-package-versions.js "$VERSION_TAG"

git add --all

git \
  -c user.name='Vaadin Bot' \
  -c user.email='vaadin-bot@users.noreply.github.com' \
  commit -m "chore(release): $VERSION_TAG"

protection_config=$(ghr -X GET)

remapped=$(node scripts/protection-remap.js "$protection_config")

ghr -X PUT -d "$(echo "$remapped" | sed '$s/"enforce_admins":true/"enforce_admins":false/')" > /dev/null

git push https://vaadin-bot:"$GITHUB_RELEASE_TOKEN"@github.com/"$REPO".git HEAD:$branch

ghr -X PUT -d "$remapped" > /dev/null
