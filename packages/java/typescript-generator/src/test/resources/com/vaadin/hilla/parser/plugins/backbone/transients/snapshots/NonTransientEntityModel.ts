import m, { StringModel } from "@vaadin/hilla-models";
import type NonTransientEntity from "./NonTransientEntity.js";
const NonTransientEntityModel = m
  .object<NonTransientEntity>("NonTransientEntity")
  .property("entityField", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("transientWithGetter", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type NonTransientEntityModel = typeof NonTransientEntityModel;
export default NonTransientEntityModel;
