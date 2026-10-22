import m, { StringModel } from "@vaadin/hilla-models";
import type CustomEntity from "./CustomEntity.js";
const CustomEntityModel = m
  .object<CustomEntity>("CustomEntity")
  .property("value", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type CustomEntityModel = typeof CustomEntityModel;
export default CustomEntityModel;
