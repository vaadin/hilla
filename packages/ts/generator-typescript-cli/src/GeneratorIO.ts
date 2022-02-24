import type File from '@hilla/generator-typescript-core/File.js';
import Plugin, { PluginConstructor } from '@hilla/generator-typescript-core/Plugin.js';
import type LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import { mkdir, readFile, rm, writeFile } from 'fs/promises';
import { createRequire } from 'module';
import { dirname, isAbsolute, join, resolve } from 'path';
import { pathToFileURL } from 'url';
import GeneratorIOException from './GeneratorIOException.js';

export default class GeneratorIO {
  public readonly cwd: string;
  readonly #logger: LoggerFactory;
  readonly #outputDir: string;
  readonly #require: NodeRequire;

  public constructor(outputDir: string, logger: LoggerFactory) {
    this.cwd = process.cwd();
    this.#outputDir = isAbsolute(outputDir) ? outputDir : resolve(this.cwd, outputDir);
    this.#logger = logger;
    this.#require = createRequire(import.meta.url);

    logger.global.info(`Output directory: ${this.#outputDir}`);
  }

  public async cleanOutputDir() {
    this.#logger.global.debug(`Cleaning ${this.#outputDir} up.`);
    await rm(this.#outputDir, { recursive: true, force: true });
    await mkdir(this.#outputDir, { recursive: true });
  }

  public async loadPlugin(modulePath: string) {
    this.#logger.global.debug(`Loading plugin: ${modulePath}`);
    const resolved = pathToFileURL(this.#require.resolve(modulePath));
    const cls: PluginConstructor = (await import(resolved.toString())).default;

    if (!Object.prototype.isPrototypeOf.call(Plugin, cls)) {
      throw new GeneratorIOException(`Plugin '${modulePath}' is not an instance of a Plugin class`);
    }

    return cls;
  }

  public async read(path: string): Promise<string> {
    this.#logger.global.debug(`Reading file: ${path}`);
    return readFile(path, 'utf8');
  }

  public async write(file: File): Promise<void> {
    const filePath = join(this.#outputDir, file.name);
    this.#logger.global.debug(`Writing file ${filePath}.`);
    const dir = dirname(filePath);
    await mkdir(dir, { recursive: true });
    return writeFile(filePath, new Uint8Array(await file.arrayBuffer()));
  }
}
