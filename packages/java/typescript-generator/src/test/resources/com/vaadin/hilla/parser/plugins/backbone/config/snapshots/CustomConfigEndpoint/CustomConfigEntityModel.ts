import m, { NumberModel, StringModel } from "@vaadin/hilla-models";
import type CustomConfigEntity from "./CustomConfigEntity.js";
const CustomConfigEntityModel = m
  .object<CustomConfigEntity>("CustomConfigEntity")
  .property("bar", m.meta(NumberModel, { jvmType: "int" }))
  .property("foo", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type CustomConfigEntityModel = typeof CustomConfigEntityModel;
export default CustomConfigEntityModel;
