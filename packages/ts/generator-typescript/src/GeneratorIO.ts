import { readFile, writeFile } from 'fs/promises';
import { URL } from 'url';
import type File from './File.js';

const cwd = process.cwd();

export default class GeneratorIO {
  readonly #outputDir: URL;

  public constructor(outputDir: string) {
    this.#outputDir = new URL(outputDir, cwd);
  }

  async read(path: string): Promise<string> {
    return readFile(new URL(path, cwd), 'utf8');
  }

  async write(path: string, data: string): Promise<void>;
  async write(files: readonly File[]): Promise<void>;
  async write(filesOrPath: readonly File[] | string, data?: string): Promise<void> {
    if (Array.isArray(filesOrPath)) {
      await Promise.all(
        filesOrPath.map(async (file) =>
          writeFile(new URL(file.name, this.#outputDir), new Uint8Array(await file.arrayBuffer())),
        ),
      );
    } else {
      await writeFile(new URL(filesOrPath as string, this.#outputDir), data!, 'utf8');
    }
  }
}
