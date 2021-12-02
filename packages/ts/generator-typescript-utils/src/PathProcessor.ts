export default class PathProcessor {
  readonly #extension: string | undefined;
  readonly #root: string | undefined;

  public constructor(root?: string, extension?: string) {
    this.#extension = extension;
    this.#root = root;
  }

  public process(path: string) {
    let result = path;

    if (this.#root && !result.startsWith(this.#root)) {
      result = `${this.#root}/${result}`;
    }

    if (this.#extension && !result.endsWith(`.${this.#extension}`)) {
      result = `${result}.${this.#extension}`;
    }

    return result;
  }
}
