export default function memoize<T>(func: () => T): () => T {
  let result: T | undefined;
  let hasResult = false;

  return () => {
    if (!hasResult) {
      result = func();
      hasResult = true;
    }
    return result!;
  };
}
