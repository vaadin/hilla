// The current package, one of the packages in the `packages` dir
import { readFile } from 'node:fs/promises';
import { basename } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const cwd = pathToFileURL(`${process.cwd()}/`);

// The current package, one of the packages in the `packages` dir
export async function loadMockConfig() {
  try {
    const content = await readFile(new URL('test/mocks/config.json', cwd), 'utf8');
    return JSON.parse(content);
  } catch {
    console.log(`No mock files found for ${basename(fileURLToPath(cwd))}. Skipping...`);
    return {};
  }
}

const mockConfig = await loadMockConfig();
let port;

export async function initialize({ port: _port }) {
  port = _port;
}

export async function resolve(specifier, context, nextResolve) {
  return specifier in mockConfig
    ? nextResolve(fileURLToPath(new URL(`test/mocks/${mockConfig[specifier]}`, cwd)))
    : nextResolve(specifier);
}
