import { posix } from 'path';
import type { SetRequired } from 'type-fest';

export type PathManagerOptions = Readonly<{
  aliasRoot?: string;
  extension?: string;
  relativeTo?: string;
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

  createRelativePath(path: string, relativeTo = this.#options.relativeTo): string {
    const { extension } = this.#options;
    let result = path;

    if (extension && !path.endsWith(extension)) {
      result = `${result}${extension}`;
    }

    result = posix.relative(relativeTo, result);
    return result.startsWith('.') ? result : `./${result}`;
  }

  createTSAliasModulePath(path: string, root = this.#options.aliasRoot): string {
    return root ? `${root}/${path}` : path;
  }
}
