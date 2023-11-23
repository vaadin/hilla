/* eslint-disable import/export */
import { AutoGrid as _AutoGrid } from './autogrid.js';
import { featureRegistration } from './util';

export * from './autogrid.js';

export const AutoGrid: typeof _AutoGrid = featureRegistration(_AutoGrid, 'AutoGrid');
