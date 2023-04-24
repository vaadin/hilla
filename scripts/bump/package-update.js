/* eslint-disable import/no-extraneous-dependencies,camelcase,no-console */
import { readFile, writeFile } from 'fs/promises';

const { npm_package_name: packageName, npm_package_json: packageFile } = process.env;

if (!packageName) {
  throw new Error('Package name should be defined');
}

if (!packageFile) {
  throw new Error('Path to package.json should be defined');
}

try {
  const packageJson = JSON.parse(await readFile(packageFile, 'utf8'));
  const { peerDependencies, version } = packageJson;

  if (peerDependencies) {
    for (const dependency of Object.keys(peerDependencies)) {
      if (dependency.startsWith('@hilla')) {
        peerDependencies[dependency] = `^${version}`;
      }
    }

    await writeFile(packageFile, `${JSON.stringify(packageJson, null, 2)}\n`, 'utf8');
  }
} catch (e) {
  throw new Error(`Error in ${packageName}`, e);
}
