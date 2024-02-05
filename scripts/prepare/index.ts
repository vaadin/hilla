/* eslint-disable no-console */
import { copyFile, mkdir, readFile, writeFile } from 'node:fs/promises';
import type { PackageJson } from 'type-fest';
import { destination, local, remote, type Versions } from './config.js';
import generate from './generate.js';

const [{ version }, versions] = await Promise.all([
  readFile(local.versionedPackageJson, 'utf-8').then(JSON.parse) as Promise<PackageJson>,
  // download needed files from vaadin/platform
  fetch(remote.versions)
    .then(async (res) => await res.text())
    .then((str) => JSON.parse(str, (_, val) => (val === '{{version}}' ? undefined : val))) as Promise<Versions>,
  mkdir(local.src, { recursive: true }),
  mkdir(local.results, { recursive: true }),
  mkdir(destination.lit.themeDir, { recursive: true }),
  mkdir(destination.react.themeDir, { recursive: true }),
]);

if (!version) {
  throw new Error('No version found in package.json of Hilla "/ts/generator-core"');
}

// run the generator
generate(version, versions);

console.log('Moving the generated files to the final place.');

await Promise.all([
  destination.lit.versions.forEach(
    file => copyFile(new URL('hilla-versions.json', local.results), file)
      .then(() => console.log(`Copied ${file.toString()}`))),
  destination.react.versions.forEach(file => copyFile(new URL('hilla-react-versions.json', local.results), file)
    .then(() => console.log(`Copied ${file.toString()}`))),
]);

const themeAnnotationsPattern = /.*(JsModule|NpmPackage).*\n/gmu;
const themeFiles = new Map([
  [remote.lumo, [new URL('Lumo.java', destination.lit.themeDir), new URL('Lumo.java', destination.react.themeDir)]],
  [
    remote.material,
    [new URL('Material.java', destination.lit.themeDir), new URL('Material.java', destination.react.themeDir)],
  ],
]);

console.log('Copying the theme files from flow-components to the final place.');

await Promise.all(
  Array.from(themeFiles.entries(), async ([url, dest]) => {
    const response = await fetch(url);
    let code = await response.text();
    code = code.replaceAll(themeAnnotationsPattern, '');
    await Promise.all(
      dest.map(async (file) => {
        await writeFile(file, code);
        console.log(`Copied ${file.toString()}`);
      }),
    );
  }),
);
