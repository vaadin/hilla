import m, { StringModel } from "@vaadin/hilla-models";
import type ComplexHierarchyModel from "./ComplexHierarchyModel.js";
import ComplexHierarchyParentModelModel from "./ComplexHierarchyParentModelModel.js";
const ComplexHierarchyModelModel = m
  .extend(ComplexHierarchyParentModelModel)
  .object<ComplexHierarchyModel>("ComplexHierarchyModel")
  .property("name", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type ComplexHierarchyModelModel = typeof ComplexHierarchyModelModel;
export default ComplexHierarchyModelModel;
