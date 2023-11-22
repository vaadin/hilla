/* eslint-disable import/export */
import { AutoForm as _AutoForm } from './autoform.js';
import { featureRegistration } from './util';

export * from './autoform.js';

export const AutoForm: typeof _AutoForm = featureRegistration(_AutoForm, 'AutoForm');
