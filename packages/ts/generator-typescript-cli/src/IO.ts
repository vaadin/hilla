import { readFile, writeFile } from 'fs/promises';
import { fileURLToPath, URL } from 'url';
import type File from '@vaadin/generator-typescript/File.js';
import Plugin, { PluginConstructor } from '@vaadin/generator-typescript/Plugin.js';
import IOException from './IOException.js';

const cwd = process.cwd();

async function load(path: URL): Promise<PluginConstructor> {
  const cls: PluginConstructor = (await import(fileURLToPath(path as URL))).default;

  if (!Object.prototype.isPrototypeOf.call(Plugin, cls)) {
    throw new IOException(`Plugin '${path}' is not an instance of a Plugin class`);
  }

  return cls;
}

function resolve(path: string): URL {
  return new URL(path, cwd);
}

export default class IO {
  readonly #outputDir: URL;

  public constructor(outputDir: string) {
    this.#outputDir = new URL(outputDir, cwd);
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

  public async read(path: string): Promise<string> {
    return readFile(resolve(path), 'utf8');
  }

  public async write(files: readonly File[]): Promise<void> {
    await Promise.all(
      files.map(async (file) =>
        writeFile(new URL(file.name, this.#outputDir), new Uint8Array(await file.arrayBuffer())),
      ),
    );
  }
}
