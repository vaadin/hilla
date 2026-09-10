import m, { ObjectModel, StringModel } from "@vaadin/hilla-models";
import FormEntityModel from "./FormEntityModel.js";
import type FormOptionalTypes from "./FormOptionalTypes.js";
const FormOptionalTypesModel: ObjectModel<FormOptionalTypes> = m
  .object<FormOptionalTypes>("FormOptionalTypes")
  .property("optionalString", StringModel)
  .property("optionalEntity", m.lazy(() => FormEntityModel))
  .property("optionalList", m.array(m.optional(StringModel)))
  .property("optionalMatrix", m.array(m.optional(m.array(m.optional(StringModel)))))
  .build();
type FormOptionalTypesModel = typeof FormOptionalTypesModel;
export default FormOptionalTypesModel;
