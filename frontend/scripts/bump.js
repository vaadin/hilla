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
  const packages = await readdir(resolve(root, 'packages'));
  await Promise.all(
    packages.map(async (_package) => {
      const indexFile = resolve(_package, 'src/index.ts');
      indexFile.replace(versionPattern, `version: /* updated-by-script */ '${version}',`);
    })
  );
}

await Promise.all([updateLernaConfig(), updatePackageRegistrations()]);
