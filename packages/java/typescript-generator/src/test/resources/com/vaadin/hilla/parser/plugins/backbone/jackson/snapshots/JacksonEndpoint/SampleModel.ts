import m, { StringModel } from "@vaadin/hilla-models";
import type Sample from "./Sample.js";
import SampleParentModel from "./SampleParentModel.js";
const SampleModel = m
  .extend(SampleParentModel)
  .object<Sample>("Sample")
  .property("publicProp", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("privateProp", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("privateTransientPropWithGetter", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("propertyGetterOnly", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("propertyWithDifferentField", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("propertySetterOnly", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("renamedPublicProp0", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("renamedPrivateProp0", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type SampleModel = typeof SampleModel;
export default SampleModel;
