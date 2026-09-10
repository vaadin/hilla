import m, { Model, NumberModel, ObjectModel } from "@vaadin/hilla-models";
import FormArrayTypesModel from "./FormArrayTypesModel.js";
import FormDataPrimitivesModel from "./FormDataPrimitivesModel.js";
import type FormEntity from "./FormEntity.js";
import FormEntityHierarchyModel from "./FormEntityHierarchyModel.js";
import FormEntityIdModel from "./FormEntityIdModel.js";
import FormEnumTypesModel from "./FormEnumTypesModel.js";
import FormNonnullTypesModel from "./FormNonnullTypesModel.js";
import FormOptionalTypesModel from "./FormOptionalTypesModel.js";
import FormRecordTypesModel from "./FormRecordTypesModel.js";
import FormTemporalTypesModel from "./FormTemporalTypesModel.js";
import FormValidationConstraintsModel from "./FormValidationConstraintsModel.js";
const FormEntityModel: ObjectModel<FormEntity> = m
  .extend(FormEntityIdModel)
  .object<FormEntity>("FormEntity")
  .property("myId", NumberModel)
  .property("dataPrimitives", FormDataPrimitivesModel)
  .property("entityHierarchy", FormEntityHierarchyModel)
  .property("temporalTypes", FormTemporalTypesModel)
  .property("arrayTypes", m.lazy(() => FormArrayTypesModel))
  .property("enumTypes", FormEnumTypesModel)
  .property("recordTypes", m.lazy(() => FormRecordTypesModel))
  .property("validationConstraints", m.lazy(() => FormValidationConstraintsModel))
  .property("myOptionalTypes", m.lazy(() => FormOptionalTypesModel))
  .property("nonnullTypes", FormNonnullTypesModel)
  .property("unknownModel", m.optional(Model))
  .build();
type FormEntityModel = typeof FormEntityModel;
export default FormEntityModel;
