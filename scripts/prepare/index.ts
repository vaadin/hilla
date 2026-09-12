/* eslint-disable no-console */
import { copyFile, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { globIterate } from 'glob';
import type { PackageJson } from 'type-fest';
import {
  componentOptions,
  destination,
  flowComponentsBranch,
  local,
  platformBranch,
  remote,
  root,
  type Versions,
} from './config.js';
import generate from './generate.js';
import { fetchNpmPackages, findNpmVersion, withNpmPackages } from './npmPackages.js';

console.log(`Fetching versions from platform branch: ${platformBranch}`);
console.log(`Fetching component npm versions from flow-components branch: ${flowComponentsBranch}`);

const [{ version }, platformVersions, componentNpmPackages] = await Promise.all([
  readFile(local.versionedPackageJson, 'utf-8').then(JSON.parse) as Promise<PackageJson>,
  // download needed files from vaadin/platform
  fetch(remote.versions)
    .then(async (res) => await res.text())
    .then((str) => JSON.parse(str, (_, val) => (val === '{{version}}' ? undefined : val))) as Promise<Versions>,
  // and the component npm versions from vaadin/flow-components, which the
  // platform no longer declares
  fetchNpmPackages(remote.componentSources),
  mkdir(local.src, { recursive: true }),
  mkdir(local.results, { recursive: true }),
]);

if (!version) {
  throw new Error('No version found in package.json of Hilla "/ts/generator-core"');
}

const versions = withNpmPackages(platformVersions, componentNpmPackages);

console.log(`Read ${[...componentNpmPackages.keys()].join(', ')} from the component annotations.`);

// The npm packages Hilla depends on itself, and therefore needs a version for.
// A package no version is found for has moved somewhere this script does not
// read, which fails the build rather than leaving the package silently at the
// version it happens to have.
const REQUIRED_NPM_PACKAGES = [
  '@vaadin/vaadin-lumo-styles',
  '@vaadin/react-components',
  '@vaadin/react-components-pro',
];
const missingNpmPackages = REQUIRED_NPM_PACKAGES.filter((npmName) => !findNpmVersion(versions, npmName));

if (missingNpmPackages.length > 0) {
  throw new Error(
    `No version found for ${missingNpmPackages.join(', ')}, neither in the platform versions nor in the component annotations. Add the file that declares the package to \`componentSources\` in scripts/prepare/config.ts.`,
  );
}

// run the generator
generate(version, versions);

console.log('Moving the generated files to the final place.');

await Promise.all(
  destination.versions.map(async (file) =>
    copyFile(new URL('hilla-versions.json', local.results), file).then(() => console.log(`Copied ${file.toString()}`)),
  ),
);

console.log('Generating components list package.json resources.');

await Promise.all(
  componentOptions.map(async (componentOption) => {
    let code = await readFile(new URL(`./${componentOption}/package.json`, local.components), 'utf-8');
    code = code.replaceAll('{{version}}', version);
    await mkdir(new URL(componentOption, destination.components), { recursive: true });
    const file = new URL(`./${componentOption}/package.json`, destination.components);
    await writeFile(file, code);
    console.log(`Generated ${file.toString()}`);
  }),
);

// Packages that are deliberately declared differently in the repository root
// than in the workspaces, and which therefore must not be propagated from it.
// The root aliases "typescript" to "@typescript/typescript6" for the tooling
// that asks for that name, so that the native TypeScript 7, installed as
// "@typescript/native", owns the "tsc" binary. The test projects model real
// applications and follow Flow's plain "typescript" instead, which is safe
// because the packages depend on "@typescript/typescript6" by name.
const NOT_PROPAGATED = new Set(['typescript']);

let rootPackageJson: PackageJson | undefined;

function updateDependencyVersion(json: PackageJson, npmName: string, versionSpec: string) {
  if (json.devDependencies?.[npmName] !== undefined) {
    json.devDependencies[npmName] = versionSpec;
  }
  if (json.dependencies?.[npmName] !== undefined) {
    json.dependencies[npmName] = versionSpec;
  }
}

async function getPackageJsonWithUpdates(file: string): Promise<PackageJson> {
  const fileUrl = new URL(file, root);
  const originalContents = (await readFile(fileUrl, 'utf-8')).trim();
  const json = JSON.parse(originalContents) as PackageJson;
  for (const packages of Object.values(versions)) {
    for (const { npmName, jsVersion } of Object.values(
      packages as Record<string, Readonly<{ npmName?: string; jsVersion?: string }>>,
    )) {
      if (!npmName || !jsVersion) {
        continue;
      }
      updateDependencyVersion(json, npmName, jsVersion);
    }
  }
  if (rootPackageJson && file !== 'package.json') {
    for (const [packageName, versionSpec] of [
      ...Object.entries(rootPackageJson.dependencies ?? {}),
      ...Object.entries(rootPackageJson.devDependencies ?? {}),
    ]) {
      if (!versionSpec || NOT_PROPAGATED.has(packageName)) {
        continue;
      }
      updateDependencyVersion(json, packageName, versionSpec);
    }
  }

  const contents = JSON.stringify(json, undefined, 2).trim();
  if (contents !== originalContents) {
    console.log(`Updating ${file}.`);
    await writeFile(fileUrl, contents, 'utf-8');
  } else {
    console.log(`Nothing to update in ${file}, skipping.`);
  }

  return json;
}

rootPackageJson = await getPackageJsonWithUpdates('package.json');

const workspaces = Array.isArray(rootPackageJson.workspaces) ? rootPackageJson.workspaces : [];

const [patterns, ignore] = workspaces.reduce<readonly [string[], string[]]>(
  ([_patterns, _ignore], pattern) => {
    if (pattern.startsWith('!')) {
      _ignore.push(`${pattern.substring(1)}/package.json`);
    } else {
      _patterns.push(`${pattern}/package.json`);
    }
    return [_patterns, _ignore];
  },
  [[], []],
);

for await (const file of globIterate(patterns, { cwd: root, ignore })) {
  await getPackageJsonWithUpdates(file);
  // Clean old IT node_modules installation
  const nodeModulesDir = new URL('node_modules/', new URL(file, root));
  await rm(nodeModulesDir, { recursive: true, force: true });
}
