/* eslint-disable no-console */
import { copyFile, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { glob } from 'glob';
import type { PackageJson } from 'type-fest';
import { componentOptions, destination, local, remote, root, type Versions } from './config.js';
import generate from './generate.js';

const [{ version }, versions] = await Promise.all([
  readFile(local.versionedPackageJson, 'utf-8').then(JSON.parse) as Promise<PackageJson>,
  // download needed files from vaadin/platform
  fetch(remote.versions)
    .then(async (res) => await res.text())
    .then((str) => JSON.parse(str, (_, val) => (val === '{{version}}' ? undefined : val))) as Promise<Versions>,
  mkdir(local.src, { recursive: true }),
  mkdir(local.results, { recursive: true }),
]);

if (!version) {
  throw new Error('No version found in package.json of Hilla "/ts/generator-core"');
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

// Known packages to synchronise with Vaadin web components version
const KNOWN_COMPONENT_PACKAGES = ['@vaadin/vaadin-themable-mixin'];
const componentsVersion = versions.core['component-base'].jsVersion ?? '';

// Known packages to synchronise with Vaadin React components version
const KNOWN_REACT_COMPONENT_PACKAGES = ['@vaadin/react-components', '@vaadin/react-components-pro'];
const reactComponentsVersion = versions.react['react-components'].jsVersion ?? '';

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
  for (const packageName of KNOWN_COMPONENT_PACKAGES) {
    updateDependencyVersion(json, packageName, componentsVersion);
  }
  for (const packageName of KNOWN_REACT_COMPONENT_PACKAGES) {
    updateDependencyVersion(json, packageName, reactComponentsVersion);
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

const rootPackageJson = await getPackageJsonWithUpdates('package.json');

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

const workspaceJsonFiles = await glob(patterns, { cwd: root, ignore });
await Promise.all(workspaceJsonFiles.map(getPackageJsonWithUpdates));
