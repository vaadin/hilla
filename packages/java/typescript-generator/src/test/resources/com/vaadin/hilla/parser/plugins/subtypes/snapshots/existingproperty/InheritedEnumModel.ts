import m from '@vaadin/hilla-models';
import type InheritedEnum from './InheritedEnum.js';
import ColourModel from './InheritedEnum/ColourModel.js';

const InheritedEnumModel = m
  .object<InheritedEnum>('InheritedEnum')
  .property('colour', m.optional(ColourModel))
  .build();
type InheritedEnumModel = typeof InheritedEnumModel;

export default InheritedEnumModel;
