import m from '@vaadin/hilla-models';
import type Marker from './Marker.js';

const MarkerModel = m
  .object<Marker>('Marker')
  .build();
type MarkerModel = typeof MarkerModel;

export default MarkerModel;
