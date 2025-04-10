/* eslint-disable no-console */
import { exec } from 'node:child_process';
import { readFile, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { parseArgs, promisify } from 'node:util';
import { globIterate as glob } from 'glob';
import { chainAsync } from 'itertools-ts/es/multi.js';
import type { PackageJson } from 'type-fest';

const {
  positionals: [reactVersion],
} = parseArgs({
  allowPositionals: true,
});

if (!reactVersion) {
  throw new Error('React version has to be specified');
}

const execAsync = promisify(exec);

const cwd = new URL('../', import.meta.url);
const globOptions = { cwd, absolute: true };

function setReactVersion(deps: PackageJson.Dependency) {
  deps.react = reactVersion;

  if (deps['react-dom']) {
    deps['react-dom'] = reactVersion;
  }
}

for await (const packageJsonPath of chainAsync(
  [fileURLToPath(new URL('package.json', cwd))],
  glob('packages/java/tests/*/package.json', globOptions),
  glob('packages/java/tests/spring/*/package.json', globOptions),
)) {
  const packageJson: PackageJson = JSON.parse(await readFile(packageJsonPath, 'utf-8'));

  if (packageJson.dependencies?.react) {
    setReactVersion(packageJson.dependencies);
  } else if (packageJson.devDependencies?.react) {
    setReactVersion(packageJson.devDependencies);
  } else {
    continue;
  }

  await writeFile(packageJsonPath, JSON.stringify(packageJson, null, 2), 'utf8');
  console.debug(`React ${reactVersion} set in ${packageJsonPath.substring(cwd.pathname.length)}`);
}

console.debug('Installing dependencies...');

const { stdout, stderr } = await execAsync('npm install');

if (stderr) {
  console.error(stderr);
}

console.debug(stdout);
