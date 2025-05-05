/* eslint-disable no-console */
import { cpSync, mkdirSync, statSync } from 'fs';
import process from 'node:process';
import { pathToFileURL } from 'node:url';
import { glob } from 'glob';
import { root } from './config';

const hoistedVitePackageDir = new URL('node_modules/vite/', root);

const cwd = pathToFileURL(process.env['INIT_CWD'] ?? process.cwd()).toString();
const cwdPathPrefix = cwd.startsWith(root.toString()) ? decodeURIComponent(cwd.substring(root.toString().length)) : '';

const workspacesToMaintain = [
  'packages/java/tests/*',
  'packages/java/tests/gradle/*',
  'packages/java/tests/spring/*'
].map(workspacePathPattern => `${workspacePathPattern}/package.json`);
const workspaceFiles = await glob(workspacesToMaintain, { cwd: root });
console.log('[vite-copy] root: ', root);
console.log('[vite-copy] cwdPathPrefix: ', cwdPathPrefix);
console.log('[vite-copy] workspaces: ', workspaceFiles);
const targetWorkspaceFiles = workspaceFiles.filter((file) => file.startsWith(cwdPathPrefix));
console.log('[vite-copy] targets: ', targetWorkspaceFiles);
for (const file of targetWorkspaceFiles) {
  const workspaceFile = new URL(file, root);
  const nodeModulesDir = new URL('node_modules/', workspaceFile);
  console.log('[vite-copy] processing workspace: ', workspaceFile);
  // Make sure Vite is findable
  mkdirSync(nodeModulesDir, { recursive: true });
  const viteCopyDir = new URL('vite/', nodeModulesDir);
  if (statSync(viteCopyDir, { throwIfNoEntry: false })) {
    console.log('[vite-copy] skipping existing Vite: ', viteCopyDir);
    continue;
  }
  console.log('[vite-copy] copy:', hoistedVitePackageDir, viteCopyDir);
  cpSync(hoistedVitePackageDir, viteCopyDir, { recursive: true });
}
