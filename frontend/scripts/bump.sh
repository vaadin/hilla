#!/usr/bin/env bash

alias ghr="curl https://api.github.com/repos/$REPO/branches/main/protection \
  -H 'Accept: application/vnd.github.v3+json' \
  -H 'Authorization: token $GITHUB_TOKEN'"

node scripts/update-package-versions.js "$VERSION_TAG"

git add --all

git \
  -c user.name='Vaadin Bot' \
  -c user.email='vaadin-bot@users.noreply.github.com' \
  commit -m "chore(release): $VERSION_TAG"

protection_config=$(ghr -X GET)

remapped=$(node scripts/protection-remap.js "$protection_config")

ghr -X PUT -d "$(sed 's/enforce_admins:\s*true/enforce_admins:false/' "$remapped")"

git push origin HEAD:main

ghr -X PUT -d "$remapped"
