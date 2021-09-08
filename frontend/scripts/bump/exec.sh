#!/usr/bin/env bash

# Fails the script if any command failed or any variable is unset
set -eu

branch=main
dir=$(dirname -- "$0")

# shellcheck disable=SC2139
alias ghr="curl https://api.github.com/repos/$REPO/branches/$branch/protection \
  -H 'Accept: application/vnd.github.v3+json' \
  -H 'Authorization: token $GIT_RELEASE_TOKEN' \
  -s"

node "$dir"/update-package-versions.js "$VERSION_TAG"

git add --all

git \
  -c user.name='Vaadin Bot' \
  -c user.email='vaadin-bot@users.noreply.github.com' \
  commit -m "chore(release): $VERSION_TAG"

protection_config=$(ghr -X GET)

remapped=$(node "$dir"/protection-remap.js "$protection_config")

# Restores the protection of the branch
restore_protection() {
  ghr -X PUT -d "$remapped" > /dev/null
  echo "[$(date -Iseconds)][info] Protection of ${branch} branch restored"
}

# Will execute "restore_protection" function in the end of the script even if
# the script exits with an error
trap "restore_protection" EXIT

< "$dir"/disabled-protection.json ghr -X PUT -d '@-' > /dev/null

git push https://vaadin-bot:"$GIT_RELEASE_TOKEN"@github.com/"$REPO".git "$branch"
