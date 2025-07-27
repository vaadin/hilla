#!/bin/bash
set -e

checked_chown() {
  local dir="$1"

  echo "Attempting chown for directory: ${dir}..."
  # Use sudo -n for non-interactive mode. Capture exit code.
  sudo -n chown -R "${USERNAME}:${USERNAME}" "${dir}"
  local command_exit_code=$?

  if [ "${command_exit_code}" == 0 ]; then
      echo "SUCCESS: Chown for ${dir} completed."
      ls -ld "${dir}"
  else
      echo "ERROR: Failed to chown ${dir}. Exit code: ${command_exit_code}"
      ls -ld "${dir}"
      exit 1
  fi
}

echo "--- Running postinstall.sh ---"
echo "Current user: $(whoami)"
echo "User ID and groups: $(id)"

# Ensure USERNAME and CONTAINER_WORKSPACE_FOLDER are available (from devcontainer.json's containerEnv)
# If these are not set, the script will fail early due to 'set -e'
if [ -z "${USERNAME}" ]; then echo "ERROR: USERNAME environment variable not set."; exit 1; fi
if [ -z "${CONTAINER_WORKSPACE_FOLDER}" ]; then echo "ERROR: CONTAINER_WORKSPACE_FOLDER environment variable not set."; exit 1; fi

# This ensures IntelliJ IDEA can write its settings, caches, and logs.
# The home directory is typically /home/<USERNAME>
checked_chown "/home/${USERNAME}"

# This ensures the user can write project-specific settings (.idea folder) and build artifacts.
# This also recursively covers node_modules if it's within the project root.
checked_chown "${CONTAINER_WORKSPACE_FOLDER}"

# Fix "detected dubious ownership" warning from Git
# https://github.com/microsoft/vscode-remote-release/issues/7628
git config --global --add safe.directory ${CONTAINER_WORKSPACE_FOLDER}

echo 'DevContainer is ready!'
