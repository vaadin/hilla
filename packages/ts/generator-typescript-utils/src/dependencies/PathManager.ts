import { posix } from 'path';
import type { SetRequired } from 'type-fest';

export type PathManagerOptions = Readonly<{
  aliasRoot?: string;
  extension?: string;
  relativeTo?: string;
}>;

export default class PathManager {
  readonly #options: SetRequired<PathManagerOptions, 'relativeTo'>;

  public constructor(options?: PathManagerOptions) {
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

  public get aliasRoot(): string | undefined {
    return this.#options.aliasRoot;
  }

  public createBareModulePath(path: string, isFile = false) {
    const { extension } = this.#options;

    if (extension && isFile) {
      return `${path}.${extension}`;
    }

    return path;
  }

  public createRelativePath(path: string, relativeTo = this.#options.relativeTo) {
    const { extension } = this.#options;
    let result = path;

    if (extension && !path.endsWith(extension)) {
      result = `${result}${extension}`;
    }

    result = posix.relative(relativeTo, result);
    return result.startsWith('.') ? result : `./${result}`;
  }

  public createTSAliasModulePath(path: string, root = this.#options.aliasRoot) {
    return root ? `${root}/${path}` : path;
  }
}
