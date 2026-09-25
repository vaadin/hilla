import m, { NumberModel, StringModel } from "@vaadin/hilla-models";
import type FormEntityHierarchy from "./FormEntityHierarchy.js";
import FormEntityIdModel from "./FormEntityIdModel.js";
const FormEntityHierarchyModel = m
  .extend(FormEntityIdModel)
  .object<FormEntityHierarchy>("FormEntityHierarchy")
  .property("lorem", StringModel)
  .property("ipsum", NumberModel)
  .build();
type FormEntityHierarchyModel = typeof FormEntityHierarchyModel;
export default FormEntityHierarchyModel;
