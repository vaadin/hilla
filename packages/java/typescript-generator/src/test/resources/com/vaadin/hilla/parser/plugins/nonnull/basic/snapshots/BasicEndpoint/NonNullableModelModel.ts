import m, { StringModel } from "@vaadin/hilla-models";
import type NonNullableModel from "./NonNullableModel.js";
const NonNullableModelModel = m
  .object<NonNullableModel>("NonNullableModel")
  .property("complexTypeField", m.meta(m.record(m.meta(m.array(m.self), { jvmType: "java.util.List" })), { jvmType: "java.util.Map" }))
  .property("nullableField", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("protectedField", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("publicField", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("typeWithTypeArgument", m.meta(m.optional(m.array(m.meta(StringModel, { jvmType: "java.lang.String" }))), { jvmType: "java.util.List" }))
  .build();
type NonNullableModelModel = typeof NonNullableModelModel;
export default NonNullableModelModel;
