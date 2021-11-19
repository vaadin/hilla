import type File from '@vaadin/generator-typescript/File.js';
import Plugin, { PluginConstructor } from '@vaadin/generator-typescript/Plugin.js';
import { mkdir, readFile, writeFile } from 'fs/promises';
import { dirname, isAbsolute, join, resolve as _resolve } from 'path';
import { fileURLToPath, pathToFileURL, URL } from 'url';
import IOException from './IOException.js';

// We have to use the specific file URL; otherwise, the dirname(process.cwd()) will be used.
const cwd = pathToFileURL(_resolve(process.cwd(), 'dummy.txt'));

async function load(path: URL): Promise<PluginConstructor> {
  const cls: PluginConstructor = (await import(path.toString())).default;

  if (!Object.prototype.isPrototypeOf.call(Plugin, cls)) {
    throw new IOException(`Plugin '${path}' is not an instance of a Plugin class`);
  }

  return cls;
}

function resolve(path: string): URL {
  return isAbsolute(path) ? pathToFileURL(path) : new URL(path, cwd);
}

export default class IO {
  readonly #outputDir: URL;

  public constructor(outputDir: string) {
    this.#outputDir = new URL(join(outputDir, 'dummy.txt'), cwd);
  }

  public async load(path: URL): Promise<PluginConstructor>;
  public async load(paths: readonly URL[]): Promise<readonly PluginConstructor[]>;
  public async load(pathOrPaths: URL | readonly URL[]): Promise<PluginConstructor | readonly PluginConstructor[]> {
    if (Array.isArray(pathOrPaths)) {
      return Promise.all(pathOrPaths.map(async (path) => load(path)));
    }

    return load(pathOrPaths as URL);
  }

  public resolve(path: string): URL;
  public resolve(paths: readonly string[]): readonly URL[];
  public resolve(pathOrPaths: string | readonly string[]): URL | readonly URL[] {
    if (Array.isArray(pathOrPaths)) {
      return pathOrPaths.map((path) => resolve(path));
    }

    return resolve(pathOrPaths as string);
  }

  public async read(path: URL): Promise<string> {
    return readFile(path, 'utf8');
  }

  public async write(files: readonly File[]): Promise<void> {
    await Promise.all(
      files.map(async (file) => {
        const url = new URL(file.name, this.#outputDir);
        const dir = dirname(fileURLToPath(url));
        await mkdir(dir, { recursive: true });
        return writeFile(url, new Uint8Array(await file.arrayBuffer()));
      }),
    );
  }
}
