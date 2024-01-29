export * from './autogrid-feature.js';
export type * from './crud.js';
export * from './autoform-feature.js';
export * from './autocrud-feature.js';
export { useDataProvider } from './data-provider.js';

// @ts-expect-error: esbuild injection
// eslint-disable-next-line @typescript-eslint/no-unsafe-call
__REGISTER__();
