import { Blob, type BlobOptions } from 'node:buffer';
import type { BinaryLike } from 'node:crypto';

export type FileOptions = Readonly<
  BlobOptions & {
    lastModified?: number;
  }
>;

export default class File extends Blob {
  readonly #lastModified?: number;
  readonly #name: string;

  constructor(fileBits: Array<BinaryLike | Blob>, fileName: string, options?: FileOptions) {
    super(fileBits, options);
    this.#name = fileName;
    this.#lastModified = options?.lastModified;
  }

  get lastModified(): number | undefined {
    return this.#lastModified;
  }

  get name(): string {
    return this.#name;
  }
}
