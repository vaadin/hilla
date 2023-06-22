import { constants } from 'node:fs';
import { access, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { createRequire } from 'node:module';
import { dirname, isAbsolute, join, resolve } from 'node:path';
import type File from '@hilla/generator-typescript-core/File.js';
import Plugin, { type PluginConstructor } from '@hilla/generator-typescript-core/Plugin.js';
import type LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import GeneratorIOException from './GeneratorIOException.js';

const require = createRequire(import.meta.url);

type PluginConstructorModule = Readonly<{
  default: PluginConstructor;
}>;

export default class GeneratorIO {
  static readonly INDEX_FILENAME = 'generated-file-list.txt';
  declare ['constructor']: typeof GeneratorIO;
  readonly cwd: string;
  readonly #logger: LoggerFactory;
  readonly #outputDir: string;

  constructor(outputDir: string, logger: LoggerFactory) {
    this.cwd = process.cwd();
    this.#outputDir = isAbsolute(outputDir) ? outputDir : resolve(this.cwd, outputDir);
    this.#logger = logger;

    logger.global.info(`Output directory: ${this.#outputDir}`);
  }

  /**
   * Cleans the output directory by removing all files that had been generated last time.
   * A list of those files is found in {@link INDEX_FILENAME}.
   * @return a set containing deleted filenames
   */
  async cleanOutputDir(): Promise<Set<string>> {
    this.#logger.global.debug(`Cleaning ${this.#outputDir} up.`);
    await mkdir(this.#outputDir, { recursive: true });
    const indexFile = resolve(this.#outputDir, this.constructor.INDEX_FILENAME);

    const deletedFiles = new Set<string>();

    try {
      const indexFileContents = await this.read(indexFile);
      const filesToDelete = indexFileContents.split('\n').filter((n) => n.length);

      await Promise.all(
        filesToDelete.map(async (filename) => {
          this.#logger.global.debug(`Deleting file ${filename}.`);
          await rm(join(this.#outputDir, filename));
          deletedFiles.add(filename);
        }),
      );

      this.#logger.global.debug(`Deleting index file ${indexFile}.`);
      await rm(indexFile);
    } catch (e) {
      // non-existing file is OK, all other errors must be rethrown
      if (!(e instanceof Error && 'code' in e && e.code === 'ENOENT')) {
        throw e;
      }
    }

    return deletedFiles;
  }

  async createFileIndex(filenames: string[]): Promise<void> {
    const path = join(this.#outputDir, this.constructor.INDEX_FILENAME);
    await writeFile(path, filenames.join('\n'), 'utf-8');
  }

  /**
   * Checks that a file exists (is visible)
   * @param path the file path to check
   */
  // eslint-disable-next-line class-methods-use-this
  async exists(path: string): Promise<boolean> {
    try {
      await access(path, constants.F_OK);
      return true;
    } catch {
      return false;
    }
  }

  async loadPlugin(modulePath: string): Promise<PluginConstructor> {
    this.#logger.global.debug(`Loading plugin: ${modulePath}`);
    const module: PluginConstructorModule = await import(require.resolve(modulePath));
    const ctr: PluginConstructor = module.default;

    if (!Object.prototype.isPrototypeOf.call(Plugin, ctr)) {
      throw new GeneratorIOException(`Plugin '${modulePath}' is not an instance of a Plugin class`);
    }

    return ctr;
  }

  async read(path: string): Promise<string> {
    this.#logger.global.debug(`Reading file: ${path}`);
    return readFile(path, 'utf8');
  }

  async write(file: File): Promise<void> {
    const filePath = join(this.#outputDir, file.name);
    this.#logger.global.debug(`Writing file ${filePath}.`);
    const dir = dirname(filePath);
    await mkdir(dir, { recursive: true });
    return writeFile(filePath, new Uint8Array(await file.arrayBuffer()));
  }
}
