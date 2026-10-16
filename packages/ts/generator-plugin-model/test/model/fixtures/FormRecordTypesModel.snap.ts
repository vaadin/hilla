import m, { Model, ObjectModel, StringModel } from "@vaadin/hilla-models";
import FormEntityHierarchyModel from "./FormEntityHierarchyModel.js";
import type FormOptionalTypes from "./FormOptionalTypes.js";
import FormOptionalTypesModel from "./FormOptionalTypesModel.js";
import type FormRecordTypes from "./FormRecordTypes.js";
const FormRecordTypesModel = m
  .object<FormRecordTypes>("FormRecordTypes")
  .property("stringMap", m.record(m.optional(StringModel)))
  .property("entityHierarchyMap", m.record(m.optional(FormEntityHierarchyModel)))
  .property("stringListMap", m.record(m.optional(m.array(m.optional(StringModel)))))
  .property("selfReferenceMap", m.record(m.optional(m.self)))
  .property("complexMap", m.record(m.optional(m.record(m.optional(m.array(m.optional(m.lazy((): ObjectModel<FormOptionalTypes> => FormOptionalTypesModel))))))))
  .property("objectMap", m.record(m.optional(Model)))
  .build();
type FormRecordTypesModel = typeof FormRecordTypesModel;
export default FormRecordTypesModel;
