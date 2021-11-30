import type File from '@vaadin/generator-typescript-core/File.js';
import Plugin, { PluginConstructor } from '@vaadin/generator-typescript-core/Plugin.js';
import { mkdir, readFile, writeFile } from 'fs/promises';
import { dirname, isAbsolute, join } from 'path';
import type Pino from 'pino';
import { fileURLToPath, pathToFileURL, URL } from 'url';
import GeneratorIOException from './GeneratorIOException.js';

export default class GeneratorIO {
  readonly #cwd: URL;
  readonly #logger: Pino.Logger;
  readonly #outputDir: URL;

  public constructor(outputDir: string, logger: Pino.Logger) {
    // We have to use the specific file URL; otherwise, the dirname(process.cwd()) will be used.
    this.#cwd = pathToFileURL(join(process.cwd(), '.gitkeep'));
    this.#outputDir = this.resolve(join(outputDir, '.gitkeep'));
    this.#logger = logger;
    logger.info(`Output directory: ${dirname(fileURLToPath(this.#outputDir))}`);
  }

  public async loadPlugin(path: URL) {
    this.#logger.debug(`Loading plugin: ${path}`);
    const cls: PluginConstructor = (await import(path.toString())).default;

    if (!Object.prototype.isPrototypeOf.call(Plugin, cls)) {
      throw new GeneratorIOException(`Plugin '${path}' is not an instance of a Plugin class`);
    }

    return cls;
  }

  public async read(path: URL): Promise<string> {
    this.#logger.debug(`Reading file: ${path}`);
    return readFile(path, 'utf8');
  }

  public resolve(path: string): URL {
    return isAbsolute(path) ? pathToFileURL(path) : new URL(path, this.#cwd);
  }

  public async write(file: File): Promise<void> {
    const url = new URL(file.name, this.#outputDir);
    this.#logger.debug(`Writing file ${fileURLToPath(url)}`);
    const dir = dirname(fileURLToPath(url));
    await mkdir(dir, { recursive: true });
    return writeFile(url, new Uint8Array(await file.arrayBuffer()));
  }
}
