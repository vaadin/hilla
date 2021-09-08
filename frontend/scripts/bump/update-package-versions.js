/* eslint-disable import/no-extraneous-dependencies,no-console */
import { readdir, readFile, writeFile } from 'fs/promises';
import meow from 'meow';
import { dirname, resolve } from 'path';
import { fileURLToPath } from 'url';

function log(message) {
  console.log(`[${new Date().toISOString()}][info]: ${message}`);
}

const {
  input: [version],
} = meow({ importMeta: import.meta });

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');

async function updatePackageRegistrations() {
  const versionPattern = /version:.+$/m;
  const packagesRoot = resolve(root, 'packages');
  // Removing folders like .DS_Store
  const packages = (await readdir(packagesRoot)).filter((dir) => !dir.startsWith('.'));

  await Promise.all(
    packages.map(async (_package) => {
      const indexFile = resolve(packagesRoot, _package, 'src/index.ts');
      const indexContent = await readFile(indexFile, 'utf8');
      const indexUpdated = indexContent.replace(versionPattern, `version: /* updated-by-script */ '${version}',`);
      await writeFile(indexFile, indexUpdated, 'utf8');

      log(`@vaadin/${_package} registration updated`);
    })
  );
}

await updatePackageRegistrations();
