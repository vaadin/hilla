/* eslint-disable import/no-extraneous-dependencies */
import { readdir, readFile, writeFile } from 'fs/promises';
import meow from 'meow';
import { dirname, resolve } from 'path';
import { fileURLToPath } from 'url';

const {
  flags: { version },
} = meow({
  importMeta: import.meta,
  flags: {
    version: {
      type: 'string',
      alias: 'v',
    },
  },
});

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');

async function updateLernaConfig() {
  const lernaConfigFile = resolve(root, 'lerna.json');
  const file = await readFile(lernaConfigFile, 'utf8');

  const config = {
    ...JSON.parse(file),
    version,
  };

  await writeFile(lernaConfigFile, JSON.stringify(config, null, 2), 'utf8');
}

async function updatePackageRegistrations() {
  const versionPattern = /version:.+$/m;
  const packagesRoot = resolve(root, 'packages');
  // Removing folders like .DS_Store
  const packages = (await readdir(packagesRoot)).filter((dir) => !dir.startsWith('.'));

  await Promise.all(
    packages.map(async (_package) => {
      const indexFile = resolve(packagesRoot, _package, 'src/index.ts');
      const content = await readFile(indexFile, 'utf8');
      const updated = content.replace(versionPattern, `version: /* updated-by-script */ '${version}',`);
      await writeFile(indexFile, updated, 'utf8');
    })
  );
}

await Promise.all([updateLernaConfig(), updatePackageRegistrations()]);
