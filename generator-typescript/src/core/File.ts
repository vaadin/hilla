import { Blob, BlobOptions } from 'node:buffer';
import type { BinaryLike } from 'node:crypto';

export type FileOptions = Readonly<
  BlobOptions & {
    lastModified?: number;
  }
>;

export default class File extends Blob {
  readonly #name: string;
  readonly #lastModified?: number;

  public constructor(fileBits: Array<BinaryLike | Blob>, fileName: string, options?: FileOptions) {
    super(fileBits, options);
    this.#name = fileName;
    this.#lastModified = options?.lastModified;
  }

  public get name(): string {
    return this.#name;
  }
}
