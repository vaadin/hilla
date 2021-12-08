import { posix } from 'path';

export default class PathManager {
  public readonly aliasRoot: string | undefined;
  readonly #extension: string | undefined;

  public constructor(extension?: string, aliasRoot?: string) {
    this.aliasRoot = aliasRoot;

    if (extension) {
      this.#extension = extension.startsWith('.') ? extension : `.${extension}`;
    }
  }

  public createBareModulePath(path: string, isFile = false) {
    if (this.#extension && isFile) {
      return `${path}.${this.#extension}`;
    }

    return path;
  }

  public createRelativePath(path: string, relativeTo = '.') {
    let result = path;

    if (this.#extension && !path.endsWith(this.#extension)) {
      result = `${result}${this.#extension}`;
    }

    result = posix.relative(relativeTo, result);
    return result.startsWith('.') ? result : `./${result}`;
  }

  public createTSAliasModulePath(path: string, root = this.aliasRoot) {
    return root ? `${root}/${path}` : path;
  }
}
