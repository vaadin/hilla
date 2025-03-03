if (!('fromAsync' in Array)) {
  const { default: fromAsync } = await import('array-from-async');
  Object.defineProperty(Array, 'fromAsync', {
    value: fromAsync,
  });
}

export {};
