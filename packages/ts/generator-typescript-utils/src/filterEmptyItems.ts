export default function filterEmptyItems<T>(arr: T[]): T extends null | undefined ? never : T[] {
  return arr.filter(Boolean) as any;
}
