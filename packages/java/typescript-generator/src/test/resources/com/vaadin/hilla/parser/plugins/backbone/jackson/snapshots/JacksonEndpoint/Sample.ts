import type SampleParent from './SampleParent.js';

interface Sample extends SampleParent {
  privateProp?: string;
  privateTransientPropWithGetter?: string;
  propertyGetterOnly?: string;
  propertySetterOnly?: string;
  propertyWithDifferentField?: string;
  publicProp?: string;
  renamedPrivateProp0?: string;
  renamedPublicProp0?: string;
}

export default Sample;
