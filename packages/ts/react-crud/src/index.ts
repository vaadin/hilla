// eslint-disable-next-line import/no-unassigned-import
import './styles.css';

export * from './autogrid.js';
export type * from './crud.js';
export * from './autoform.js';
export * from './autocrud.js';

// @ts-expect-error: esbuild injection
// eslint-disable-next-line @typescript-eslint/no-unsafe-call
__REGISTER__();
