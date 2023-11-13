#!/usr/bin/env node
/**
 * This script is used to create PRs updating `package.json`
 * and `package-lock.json` files in test modules targeting the default and
 * maintained branches.
 *
 * If the git working copy has changes in these files, the changes
 * are committed into a temporary branch, and the PR is created
 * targeting the original branch. Otherwise, does nothing.
 *
 */

const https = require("https");
const exec = require('util').promisify(require('child_process').exec);
const { sep } = require('path');


const repo = "vaadin/hilla";
const token = process.env['GITHUB_TOKEN'];
if (!token) {
  console.log(`GITHUB_TOKEN is not set, skipping PR creation`);
  process.exit(1);
}

/**
 * Checks if the given file path is for `package.json` or `package-lock.json`.
 *
 * @param path {string} the file path to check
 * @return {boolean}
 */
function isPackageJsonOrLockFile(path) {
  const filename = path.split(sep).slice(-1)[0];
  return filename === 'package.json' || filename === 'package-lock.json';
}

/**
 * Commits the given files in a temporary branch and creates a PR
 * @param paths {string[]} the files
 * @return {Promise<void>}
 */
async function updateFilesWithPR(paths){
  const originalBranchName = (await exec('git rev-parse --abbrev-ref HEAD')).stdout.trim();
  const branchName = `chore/${originalBranchName}/update-package-lock-json`;

  const title = `chore: update package[-lock].json`;
  const titleForPR = `${title} (${originalBranchName})`;

  await exec(`git checkout -b ${branchName}`);
  try {
    for (const path of paths) {
      await exec(`git add "${path}"`);
    }
    await exec(`git commit -m "${title}"`);
    if ((await exec(`git ls-remote --heads origin ${branchName}`)).stdout.length) {
      console.log(`Remote branch ${branchName} exists, force pushing to update.`);
      await exec(`git push --force origin HEAD:${branchName}`);
    } else {
      await exec(`git push origin HEAD:${branchName}`);
      await createPR(titleForPR, branchName, originalBranchName);
    }
  } finally {
    await exec(`git checkout ${originalBranchName}`);
    await exec(`git branch -D ${branchName}`);
  }
}

async function createPR(title, head, base){
  const payload = {title, head, base};

  return new Promise(resolve => {
    const content = JSON.stringify({ title, head, base }, null, 1)
    const req = https.request({
      method: 'POST',
      hostname: 'api.github.com',
      path: `/repos/${repo}/pulls`,
      headers: {
        'Authorization': `token ${token}`,
        'User-Agent': 'Vaadin Cherry Pick',
        'Content-Type': 'application/json',
        'Content-Length': content.length,
      },
      body: content
    }, res => {
      let body = "";
      res.on("data", data => {
        body += data;
      });
      res.on("end", () => {
        resolve(body);
      });
    });
    req.write(content)
  }).then(body => {
    const resp = JSON.parse(body);
    console.log(`Created PR '${title}' ${resp.url}`);
  });
}


/**
 * Main async script entrypoint.
 *
 * @return {Promise<void>}
 */
async function main(){
  const isIndexClean = (await exec('git diff-index --cached --name-only HEAD')).stdout === '';
  if (!isIndexClean) {
    throw new Error('Index is not clean. Please commit or reset git index.');
  }

  const modifiedPaths = (await exec('git diff-files --name-only --diff-filter=M')).stdout.trim().split('\n');
  const modifiedPathsToUpdate = modifiedPaths.filter(isPackageJsonOrLockFile);
  if (modifiedPathsToUpdate.length) {
    console.log(`Creating PR to update files: \n  ${modifiedPathsToUpdate.join('\n  ')}`);
    await updateFilesWithPR(modifiedPathsToUpdate);
  } else {
    console.log('No package[-lock].json files were modified, skipping.')
  }
}

main().then(
  () => {
    process.exit(0);
  },
  (err) => {
    console.error(err);
    process.exit(1);
  }
);
