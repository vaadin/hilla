#!/usr/bin/env bash

# Fails the script if any command failed or any variable is unset
set -eu

branch=main
bump_scripts_dir=$(dirname -- "$0")
packages_dir="$PWD/packages/ts"

# shellcheck disable=SC2139
alias ghr="curl https://api.github.com/repos/$REPO/branches/$branch/protection \
  -H 'Accept: application/vnd.github.v3+json' \
  -H 'Authorization: token $GIT_RELEASE_TOKEN' \
  -s"

# Updating the registration version for all packages
find "$packages_dir/*/src/index.ts" -exec sed -i "s/version:.\+\$/version: \/* updated-by-script *\/ \'$VERSION_TAG\',/m" {} +

npx lerna version "$VERSION_TAG" --no-git-tag-version --no-push --yes

git add --all

git \
  -c user.name='Vaadin Bot' \
  -c user.email='vaadin-bot@users.noreply.github.com' \
  commit -m "chore(release): $VERSION_TAG"

protection_config=$(ghr -X GET)

remapped=$(node "$bump_scripts_dir/protection-remap.js" "$protection_config")

# Restores the protection of the branch
restore_protection() {
  ghr -X PUT -d "$remapped" > /dev/null
  echo "[$(date -Iseconds)][info] Protection of ${branch} branch restored"
}

# Will execute "restore_protection" function in the end of the script even if
# the script exits with an error
trap "restore_protection" EXIT

< "$bump_scripts_dir/disabled-protection.json" ghr -X PUT -d '@-' > /dev/null

git push "https://vaadin-bot:$GIT_RELEASE_TOKEN@github.com/$REPO.git" "$branch"
