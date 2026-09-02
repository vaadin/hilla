import type SampleParent_1 from "./SampleParent.js";
interface Sample extends SampleParent_1 {
    publicProp?: string;
    privateProp?: string;
    privateTransientPropWithGetter?: string;
    propertyGetterOnly?: string;
    propertyWithDifferentField?: string;
    propertySetterOnly?: string;
    renamedPublicProp0?: string;
    renamedPrivateProp0?: string;
}
export default Sample;
