/* eslint-disable no-console */
import { cpSync, mkdirSync, statSync } from 'fs';
import process from 'node:process';
import { pathToFileURL } from 'node:url';
import { root, workspaceFiles } from './config';

console.group('vite-copy.ts:');

const hoistedVitePackageDir = new URL('node_modules/vite/', root);

const cwd = process.env['INIT_CWD'] ?? process.cwd();
console.log(`Detected working directory: ${cwd}`);
const cwdUrl = pathToFileURL(cwd).toString();
const cwdPathPrefix = cwd.startsWith(root.toString())
  ? decodeURIComponent(cwdUrl.substring(root.toString().length))
  : '';

const targetWorkspaceFiles = workspaceFiles.filter((file) => file.startsWith(cwdPathPrefix));
console.log({ cwdUrl, root: root.toString(), cwdPathPrefix, workspaceFiles, targetWorkspaceFiles });
for (const file of targetWorkspaceFiles) {
  const workspaceFile = new URL(file, root);
  const nodeModulesDir = new URL('node_modules/', workspaceFile);
  // Make sure Vite is findable
  mkdirSync(nodeModulesDir, { recursive: true });
  const viteCopyDir = new URL('vite/', nodeModulesDir);
  console.log(`Checking ${viteCopyDir.toString()}...`);
  if (statSync(viteCopyDir, { throwIfNoEntry: false })) {
    console.log('...already exists, skipping.');
    continue;
  }
  console.log(`...copyping from ${hoistedVitePackageDir.toString()} to ${viteCopyDir.toString()}.`);
  cpSync(hoistedVitePackageDir, viteCopyDir, { recursive: true });
}

console.groupEnd();
