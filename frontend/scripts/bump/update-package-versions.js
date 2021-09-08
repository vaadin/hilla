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

async function updateLernaConfig() {
  const lernaConfigFile = resolve(root, 'lerna.json');
  const file = await readFile(lernaConfigFile, 'utf8');

  const config = {
    ...JSON.parse(file),
    version,
  };

  await writeFile(lernaConfigFile, JSON.stringify(config, null, 2), 'utf8');

  log('lerna.json updated');
}

async function updatePackageRegistrations() {
  const versionPattern = /version:.+$/m;
  const packagesRoot = resolve(root, 'packages');
  // Removing folders like .DS_Store
  const packages = (await readdir(packagesRoot)).filter((dir) => !dir.startsWith('.'));

  await Promise.all(
    packages.map(async (_package) => {
      const indexFile = resolve(packagesRoot, _package, 'src/index.ts');
      const packageFile = resolve(packagesRoot, _package, 'package.json');
      const [indexContent, packageContent] = await Promise.all([
        readFile(indexFile, 'utf8'),
        readFile(packageFile, 'utf8'),
      ]);
      const indexUpdated = indexContent.replace(versionPattern, `version: /* updated-by-script */ '${version}',`);
      const packageUpdated = JSON.stringify({ ...JSON.parse(packageContent), version }, null, 2);
      await Promise.all([writeFile(indexFile, indexUpdated, 'utf8'), writeFile(packageFile, packageUpdated, 'utf8')]);

      log(`@vaadin/${_package} version and registration updated`);
    })
  );
}

await Promise.all([updateLernaConfig(), updatePackageRegistrations()]);
