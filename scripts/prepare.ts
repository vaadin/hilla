/* eslint-disable no-console */
import { copyFile, mkdir, readFile, writeFile } from 'node:fs/promises';
import type { PackageJson } from 'type-fest';
import { destination, local, remote, type Versions } from './config.js';
import generate from './generator/generate.js';

const [{ version }, versions] = await Promise.all([
  readFile(local.versionedPackageJson, 'utf-8').then(JSON.parse) as Promise<PackageJson>,
  // download needed files from vaadin/platform
  fetch(remote.versions)
    .then(async (res) => await res.text())
    .then((str) => JSON.parse(str, (_, val) => (val === '{{value}}' ? undefined : val))) as Promise<Versions>,
  mkdir(local.src, { recursive: true }),
  mkdir(local.results, { recursive: true }),
  mkdir(destination.lit.themeDir, { recursive: true }),
  mkdir(destination.react.themeDir, { recursive: true }),
]);

if (!version) {
  throw new Error('No version found in package.json of Hilla "generator-typescript-core"');
}

// run the generator
generate(version, versions);

// copy generated poms to the final place
await Promise.all([
  copyFile(new URL('hilla-versions.json', local.results), destination.lit.versions),
  copyFile(new URL('hilla-react-versions.json', local.results), destination.react.versions),
]);

console.log('Copied the theme file from flow-components to hilla and hilla-react');

const themeAnnotationsPattern = /.*(JsModule|NpmPackage).*\n/gmu;
const themeFiles = new Map([
  [remote.lumo, [new URL('Lumo.java', destination.lit.themeDir), new URL('Lumo.java', destination.react.themeDir)]],
  [
    remote.material,
    [new URL('Material.java', destination.lit.themeDir), new URL('Material.java', destination.react.themeDir)],
  ],
]);

await Promise.all(
  Array.from(themeFiles.entries(), async ([url, dest]) => {
    const response = await fetch(url);
    let code = await response.text();
    code = code.replaceAll(themeAnnotationsPattern, '');
    await Promise.all(
      dest.map(async (file) => {
        console.log(file.toString(), code);
        await writeFile(file, code);
      }),
    );
  }),
);
