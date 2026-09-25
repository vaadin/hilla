import m, { StringModel } from "@vaadin/hilla-models";
import type SampleParent from "./SampleParent.js";
const SampleParentModel = m
  .object<SampleParent>("SampleParent")
  .property("publicParentProperty", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("privateParentProperty", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type SampleParentModel = typeof SampleParentModel;
export default SampleParentModel;
