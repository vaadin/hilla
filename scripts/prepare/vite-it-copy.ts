import { cp, mkdir } from 'node:fs/promises';
import { root, workspaceFiles } from './config';

const hoistedVitePackageDir = new URL('node_modules/vite/', root);
await Promise.all(
  workspaceFiles.map(async (file) => {
    const workspaceFile = new URL(file, root);

    const nodeModulesDir = new URL('node_modules/', workspaceFile);
    // Make sure Vite is findable
    await mkdir(nodeModulesDir, { recursive: true });
    const viteCopyDir = new URL('vite/', nodeModulesDir);
    try {
      await cp(hoistedVitePackageDir, viteCopyDir, { recursive: true });
    } catch (err) {
      if (typeof err === 'object') {
        const errorObject = err ?? {};
        if ('code' in errorObject && errorObject.code === 'EEXIST') {
          // ignore existing installation
        }
      }
    }
  }),
);
