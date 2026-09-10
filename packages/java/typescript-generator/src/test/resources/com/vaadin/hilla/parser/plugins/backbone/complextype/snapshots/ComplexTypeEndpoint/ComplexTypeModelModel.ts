import m, { StringModel } from "@vaadin/hilla-models";
import type ComplexTypeModel from "./ComplexTypeModel.js";
const ComplexTypeModelModel = m
  .object<ComplexTypeModel>("ComplexTypeModel")
  .property("complexList", m.meta(m.optional(m.array(m.meta(m.optional(m.record(m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))), { jvmType: "java.util.List" }))), { jvmType: "java.util.Map" }))), { jvmType: "java.util.List" }))
  .property("complexMap", m.meta(m.optional(m.record(m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))), { jvmType: "java.util.List" }))), { jvmType: "java.util.Map" }))
  .build();
type ComplexTypeModelModel = typeof ComplexTypeModelModel;
export default ComplexTypeModelModel;
