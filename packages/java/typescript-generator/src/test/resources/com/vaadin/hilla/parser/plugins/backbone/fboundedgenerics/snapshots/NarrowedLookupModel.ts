import m from '@vaadin/hilla-models';
import EnumLookupValueModel from './EnumLookupValueModel.js';
import type NarrowedLookup from './NarrowedLookup.js';

const NarrowedLookupModel = m
  .object<NarrowedLookup>('NarrowedLookup')
  .property('value', m.optional(EnumLookupValueModel))
  .build();
type NarrowedLookupModel = typeof NarrowedLookupModel;

export default NarrowedLookupModel;
