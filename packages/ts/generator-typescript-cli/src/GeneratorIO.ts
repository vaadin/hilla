import type File from '@hilla/generator-typescript-core/File.js';
import Plugin, { type PluginConstructor } from '@hilla/generator-typescript-core/Plugin.js';
import type LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import { access, mkdir, readFile, rm, writeFile } from 'fs/promises';
import { createRequire } from 'module';
import { dirname, isAbsolute, join, resolve } from 'path';
import { pathToFileURL } from 'url';
import { constants } from 'fs';
import GeneratorIOException from './GeneratorIOException.js';

export default class GeneratorIO {
  public readonly cwd: string;
  static readonly INDEX_FILENAME = 'generated-file-list.txt';
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

  /**
   * Cleans the output directory by removing all files that had been generated last time.
   * A list of those files is found in {@link GeneratorIO.INDEX_FILENAME}.
   * @return a set containing deleted filenames
   */
  public async cleanOutputDir(): Promise<Set<string>> {
    this.#logger.global.debug(`Cleaning ${this.#outputDir} up.`);
    await mkdir(this.#outputDir, { recursive: true });
    const indexFile = resolve(this.#outputDir, GeneratorIO.INDEX_FILENAME);
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
    } catch (err: any) {
      // non-existing file is OK, all other errors must be rethrown
      if (err.code !== 'ENOENT') {
        throw err;
      }
    }

    return deletedFiles;
  }

  public async createFileIndex(filenames: string[]) {
    const path = join(this.#outputDir, GeneratorIO.INDEX_FILENAME);
    await writeFile(path, filenames.join('\n'), 'utf-8');
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

  /**
   * Checks that a file exists (is visible)
   * @param path the file path to check
   */
  public exists(path: string): Promise<boolean> {
    return new Promise((res, _rej) => {
      return access(path, constants.F_OK).then(
        () => {
          res(true);
        },
        () => {
          res(false);
        },
      );
    });
  }
}
