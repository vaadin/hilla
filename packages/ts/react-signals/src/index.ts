import { installAutoSignalTracking } from '@preact/signals-react/runtime';

// eslint-disable-next-line @typescript-eslint/no-unsafe-call
installAutoSignalTracking();

export * from '@preact/signals-react';
export { NumberSignalChannel } from './EventChannel';
export * from './types.js';
