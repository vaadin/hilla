// TODO: Remove this polyfill when we move to ECMA2024
declare global {
  interface PromiseConstructor {
    withResolvers<T>(): {
      resolve(value: T): void;
      reject(reason?: unknown): void;
      promise: Promise<T>;
    };
  }
}

if (!('withResolvers' in Promise)) {
  // eslint-disable-next-line no-extend-native
  Object.defineProperty(Promise, 'withResolvers', {
    configurable: true,
    value<T>() {
      let resolve: (value: T) => void;
      let reject: (reason?: unknown) => void;
      const promise = new Promise<T>((_resolve, _reject) => {
        resolve = _resolve;
        reject = _reject;
      });
      return { resolve: resolve!, reject: reject!, promise };
    },
  });
}

export {};
