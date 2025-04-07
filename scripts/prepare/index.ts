/* eslint-disable no-console */
import { copyFile, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import type { PackageJson } from 'type-fest';
import { componentOptions, destination, local, remote, root, type Versions, workspaceFiles } from './config.js';
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

const reactComponentsPackageName = '@vaadin/react-components';
const reactComponentsProPackageName = '@vaadin/react-components-pro';
const reactComponentsVersion = versions.react['react-components'].jsVersion ?? '';
const reactComponentsSpec = `${reactComponentsPackageName}@${reactComponentsVersion}`;
const rootPackageJsonFile = new URL('package.json', root);
const rootPackageJson = JSON.parse(await readFile(rootPackageJsonFile, 'utf-8')) as PackageJson;
if ((rootPackageJson.devDependencies ??= {})[reactComponentsPackageName] !== reactComponentsVersion) {
  console.log(`Updating "${reactComponentsSpec}".`);
  // Update hoisted version in root package.json
  rootPackageJson.devDependencies[reactComponentsPackageName] = reactComponentsVersion;
  // Also update hoisted pro version
  rootPackageJson.devDependencies[reactComponentsProPackageName] = reactComponentsVersion;
  await writeFile(rootPackageJsonFile, JSON.stringify(rootPackageJson, undefined, 2), 'utf-8');

  // Keep @vaadin/hilla-react-crud in sync
  const reactCrudPackageJsonFile = new URL('packages/ts/react-crud/package.json', root);
  const reactCrudPackageJson = JSON.parse(await readFile(reactCrudPackageJsonFile, 'utf-8')) as PackageJson;
  (reactCrudPackageJson.dependencies ??= {})[reactComponentsPackageName] = reactComponentsVersion;
  await writeFile(reactCrudPackageJsonFile, JSON.stringify(reactCrudPackageJson, undefined, 2), 'utf-8');
} else {
  console.log(`Skipping install for "${reactComponentsSpec}", same version installed.`);
}

function updateDependencyVersion(packageJson: PackageJson, npmName: string, versionSpec: string) {
  if (packageJson.devDependencies?.[npmName] !== undefined) {
    packageJson.devDependencies[npmName] = versionSpec;
  }
  if (packageJson.dependencies?.[npmName] !== undefined) {
    packageJson.dependencies[npmName] = versionSpec;
  }
}
await Promise.all(
  workspaceFiles.map(async (file) => {
    const workspaceFile = new URL(file, root);
    const workspaceJsonContents = (await readFile(workspaceFile, 'utf-8')).trim();
    const workspaceJson = JSON.parse(workspaceJsonContents) as PackageJson;
    // Update versions in workspace package.json from versions.json
    for (const packages of Object.values(versions)) {
      for (const { npmName, jsVersion } of Object.values(
        packages as Record<string, Readonly<{ npmName?: string; jsVersion?: string }>>,
      )) {
        if (!npmName || !jsVersion) {
          continue;
        }
        updateDependencyVersion(workspaceJson, npmName, jsVersion);
      }
    }
    // FIXME: replace with correct source for themable mixin version
    updateDependencyVersion(workspaceJson, '@vaadin/vaadin-themable-mixin', reactComponentsVersion);
    // Update versions in workspace package.json from root
    for (const [npmName, jsVersion] of Object.entries(rootPackageJson.devDependencies ?? {})) {
      if (!jsVersion) {
        continue;
      }
      updateDependencyVersion(workspaceJson, npmName, jsVersion);
    }
    const contents = JSON.stringify(workspaceJson, undefined, 2).trim();
    if (contents !== workspaceJsonContents) {
      console.log(`Updating ${file}.`);
      await writeFile(workspaceFile, contents, 'utf-8');
    } else {
      console.log(`Nothing to update in ${file}, skipping.`);
    }
    // Clean old IT node_modules installation
    const nodeModulesDir = new URL('node_modules/', workspaceFile);
    await rm(nodeModulesDir, { recursive: true, force: true });
  }),
);
