export function createId(): number {
  const buf = new Uint32Array(1);
  crypto.getRandomValues(buf);
  return buf[0];
}

export const ZERO_ID = 0;

export class InconsistentTreeError extends Error {
  constructor(id: number) {
    super(`Inconsistent tree: missing node with id ${id}`);
  }
}
