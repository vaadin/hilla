import m, { NumberModel } from "@vaadin/hilla-models";
import type ComplexHierarchyGrandParentModel from "./ComplexHierarchyGrandParentModel.js";
const ComplexHierarchyGrandParentModelModel = m
  .object<ComplexHierarchyGrandParentModel>("ComplexHierarchyGrandParentModel")
  .property("build", m.meta(NumberModel, { jvmType: "int" }))
  .build();
type ComplexHierarchyGrandParentModelModel = typeof ComplexHierarchyGrandParentModelModel;
export default ComplexHierarchyGrandParentModelModel;
