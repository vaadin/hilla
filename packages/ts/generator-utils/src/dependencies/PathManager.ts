import { basename, dirname, extname, posix } from 'node:path';
import { fileURLToPath } from 'node:url';
import type { SetRequired } from 'type-fest';

export type PathManagerOptions = Readonly<{
  aliasRoot?: string;
  extension?: string;
  relativeTo?: URL | string;
}>;

export default class PathManager {
  readonly #options: SetRequired<PathManagerOptions, 'relativeTo'>;

  constructor(options?: PathManagerOptions) {
    let extension: string | undefined;

    if (options?.extension) {
      extension = options.extension.startsWith('.') ? options.extension : `.${options.extension}`;
    }

    this.#options = {
      ...options,
      extension,
      relativeTo: options?.relativeTo ?? '.',
    };
  }

  get aliasRoot(): string | undefined {
    return this.#options.aliasRoot;
  }

  createBareModulePath(path: string, isFile = false): string {
    const { extension } = this.#options;

    if (extension && isFile) {
      return `${path}.${extension}`;
    }

    return path;
  }

  createRelativePath(path: URL | string, fileExtension?: string, relativeTo = this.#options.relativeTo): string {
    const { extension } = this.#options;
    const _path = path instanceof URL ? fileURLToPath(path) : path;
    let result = _path;

    if (extension && !_path.endsWith(extension)) {
      result = `${dirname(result)}/${basename(result, fileExtension)}${extension}`;
    }

    result = posix.relative(relativeTo instanceof URL ? fileURLToPath(relativeTo) : relativeTo, result);
    return result.startsWith('.') ? result : `./${result}`;
  }

  createTSAliasModulePath(path: string, root = this.#options.aliasRoot): string {
    return root ? `${root}/${path}` : path;
  }
}
