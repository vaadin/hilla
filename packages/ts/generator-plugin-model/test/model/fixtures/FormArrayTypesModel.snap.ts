import m, { NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-models";
import type FormArrayTypes from "./FormArrayTypes.js";
import type FormEntity from "./FormEntity.js";
import FormEntityHierarchyModel from "./FormEntityHierarchyModel.js";
import FormEntityModel from "./FormEntityModel.js";
const FormArrayTypesModel = m
  .object<FormArrayTypes>("FormArrayTypes")
  .property("stringList", m.array(m.optional(StringModel)))
  .property("entityHierarchyList", m.array(m.optional(FormEntityHierarchyModel)))
  .property("selfReferenceList", m.array(m.optional(m.self)))
  .property("stringArray", m.array(m.optional(StringModel)))
  .property("numberMatrix", m.array(m.optional(m.array(m.optional(NumberModel)))))
  .property("entityMatrix", m.array(m.optional(m.array(m.optional(m.lazy((): ObjectModel<FormEntity> => FormEntityModel))))))
  .property("nestedArrays", m.array(m.optional(m.array(m.optional(m.record(m.optional(m.array(m.optional(StringModel)))))))))
  .build();
type FormArrayTypesModel = typeof FormArrayTypesModel;
export default FormArrayTypesModel;
