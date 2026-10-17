import m, { StringModel } from "@vaadin/hilla-models";
import type NestedEntity from "./NestedEntity.js";
const NestedEntityModel = m
  .object<NestedEntity>("NestedEntity")
  .property("name", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type NestedEntityModel = typeof NestedEntityModel;
export default NestedEntityModel;
