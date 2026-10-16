import m from '@vaadin/hilla-models';
import type Empty from './Empty.js';

const EmptyModel = m
  .object<Empty>('Empty')
  .build();
type EmptyModel = typeof EmptyModel;

export default EmptyModel;
