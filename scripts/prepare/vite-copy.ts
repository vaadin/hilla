/* eslint-disable no-console */
import { cpSync, mkdirSync } from 'fs';
import process from 'node:process';
import { pathToFileURL } from 'node:url';
import { root, workspaceFiles } from './config';

const hoistedVitePackageDir = new URL('node_modules/vite/', root);

const cwd = pathToFileURL(process.env['INIT_CWD'] ?? process.cwd()).toString();
const cwdPathPrefix = cwd.startsWith(root.toString()) ? decodeURIComponent(cwd.substring(root.toString().length)) : '';

const targetWorkspaceFiles = workspaceFiles.filter((file) => file.startsWith(cwdPathPrefix));
for (const file of targetWorkspaceFiles) {
  const workspaceFile = new URL(file, root);
  const nodeModulesDir = new URL('node_modules/', workspaceFile);
  // Make sure Vite is findable
  mkdirSync(nodeModulesDir, { recursive: true });
  const viteCopyDir = new URL('vite/', nodeModulesDir);
  try {
    cpSync(hoistedVitePackageDir, viteCopyDir, { recursive: true });
  } catch (err) {
    if (typeof err === 'object' && (err as { readonly code: string }).code === 'EEXIST') {
      // ignore errors caused by existing installation
    } else {
      throw err;
    }
  }
}
