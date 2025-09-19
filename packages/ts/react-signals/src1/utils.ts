import type { UnionToTuple } from 'type-fest';

export type ObjectValueTuple<T, KS extends any[] = UnionToTuple<keyof T>, R extends any[] = []> = KS extends [
  infer K,
  ...infer KT,
]
  ? ObjectValueTuple<T, KT, [...R, T[K & keyof T]]>
  : R;

export function createId(): number {
  const buf = new Uint32Array(1);
  crypto.getRandomValues(buf);
  return buf[0];
}

export const ZERO_ID = 0;
