/* eslint-disable no-console */
import { spawn, type SpawnOptions } from 'node:child_process';
import { copyFile, mkdir, readFile, writeFile } from 'node:fs/promises';
import type { PackageJson } from 'type-fest';
import { componentOptions, destination, local, remote, root, type Versions } from './config.js';
import generate from './generate.js';

async function run(command: string, args: string[] = [], options: SpawnOptions = {}): Promise<void> {
  return new Promise((resolve, reject) => {
    console.log(`> ${command} ${args.join(' ')}`);
    const childProcess = spawn(command, args, {
      shell: true,
      stdio: 'inherit',
      ...options,
    });
    childProcess.on('exit', (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`Child process exited with non-zero exit code ${code}`));
      }
    });
  });
}

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
const packageJson = JSON.parse(await readFile(rootPackageJsonFile, 'utf-8')) as PackageJson;
if ((packageJson.devDependencies ??= {})[reactComponentsPackageName] !== reactComponentsVersion) {
  console.log(`Updating "${reactComponentsSpec}".`);
  // Update hoisted version in root package.json
  packageJson.devDependencies[reactComponentsPackageName] = reactComponentsVersion;
  // Also update hoisted pro version
  packageJson.devDependencies[reactComponentsProPackageName] = reactComponentsVersion;
  await writeFile(rootPackageJsonFile, JSON.stringify(packageJson, undefined, 2), 'utf-8');

  // Keep @vaadin/hilla-react-crud in sync
  const reactCrudPackageJsonFile = new URL('packages/ts/react-crud/package.json', root);
  const reactCrudPackageJson = JSON.parse(await readFile(reactCrudPackageJsonFile, 'utf-8')) as PackageJson;
  (reactCrudPackageJson.dependencies ??= {})[reactComponentsPackageName] = reactComponentsVersion;
  await writeFile(reactCrudPackageJsonFile, JSON.stringify(reactCrudPackageJson, undefined, 2), 'utf-8');
} else {
  console.log(`Skipping install for "${reactComponentsSpec}", same version installed.`);
}
