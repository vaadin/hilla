import m, { StringModel } from "@vaadin/hilla-models";
import type Dependency from "./Dependency.js";
const DependencyModel = m
  .object<Dependency>("Dependency")
  .property("defaultField", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("nullableField", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("nullableSignatureField", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type DependencyModel = typeof DependencyModel;
export default DependencyModel;
