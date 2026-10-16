import m, { StringModel } from "@vaadin/hilla-models";
import type SubPackageDependency from "./SubPackageDependency.js";
const SubPackageDependencyModel = m
  .object<SubPackageDependency>("SubPackageDependency")
  .property("defaultField", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type SubPackageDependencyModel = typeof SubPackageDependencyModel;
export default SubPackageDependencyModel;
