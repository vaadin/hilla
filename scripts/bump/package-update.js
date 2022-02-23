/* eslint-disable import/no-extraneous-dependencies,camelcase,no-console */
import meow from 'meow';
import { readFile, writeFile } from 'fs/promises';

const {
  input: packageFileNames,
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

const errors = new Map();

await Promise.all(
  packageFileNames.map(async (packageFileName) => {
    try {
      const packageJson = JSON.parse(await readFile(packageFileName, 'utf8'));
      const { peerDependencies } = packageJson;

      if (peerDependencies) {
        for (const dependency of Object.keys(peerDependencies)) {
          if (dependency.startsWith('@hilla')) {
            peerDependencies[dependency] = `^${version}`;
          }
        }

        await writeFile(packageFileName, `${JSON.stringify(packageJson, null, 2)}\n`, 'utf8');
      }
    } catch (e) {
      errors.set(packageFileName, e);
    }
  }),
);

for (const [file, error] of errors) {
  console.error(`Error in ${file}:\n`, error);
}
