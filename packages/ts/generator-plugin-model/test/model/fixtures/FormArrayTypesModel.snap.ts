import m, { NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-models";
import type FormArrayTypes from "./FormArrayTypes.js";
import FormEntityHierarchyModel from "./FormEntityHierarchyModel.js";
import FormEntityModel from "./FormEntityModel.js";
const FormArrayTypesModel: ObjectModel<FormArrayTypes> = m
  .object<FormArrayTypes>("FormArrayTypes")
  .property("stringList", m.array(m.optional(StringModel)))
  .property("entityHierarchyList", m.array(m.optional(FormEntityHierarchyModel)))
  .property("selfReferenceList", m.array(m.optional(m.self)))
  .property("stringArray", m.array(m.optional(StringModel)))
  .property("numberMatrix", m.array(m.optional(m.array(m.optional(NumberModel)))))
  .property("entityMatrix", m.array(m.optional(m.array(m.optional(m.lazy(() => FormEntityModel))))))
  .property("nestedArrays", m.array(m.optional(m.array(m.optional(m.record(m.optional(m.array(m.optional(StringModel)))))))))
  .build();
type FormArrayTypesModel = typeof FormArrayTypesModel;
export default FormArrayTypesModel;
