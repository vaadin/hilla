import m, { StringModel } from "@vaadin/hilla-models";
import ComplexHierarchyGrandParentModelModel from "./ComplexHierarchyGrandParentModelModel.js";
import type ComplexHierarchyParentModel from "./ComplexHierarchyParentModel.js";
const ComplexHierarchyParentModelModel = m
  .extend(ComplexHierarchyGrandParentModelModel)
  .object<ComplexHierarchyParentModel>("ComplexHierarchyParentModel")
  .property("id", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type ComplexHierarchyParentModelModel = typeof ComplexHierarchyParentModelModel;
export default ComplexHierarchyParentModelModel;
