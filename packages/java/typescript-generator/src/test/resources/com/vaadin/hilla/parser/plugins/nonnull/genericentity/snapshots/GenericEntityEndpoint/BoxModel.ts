import m, { Model } from '@vaadin/hilla-models';
import type Box from './Box.js';

const BoxModel = m
  .object<Box>('Box')
  .property('content', m.optional(Model))
  .build();
type BoxModel = typeof BoxModel;

export default BoxModel;
