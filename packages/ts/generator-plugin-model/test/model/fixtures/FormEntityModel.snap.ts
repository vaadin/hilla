import m, { Model, NumberModel, ObjectModel } from "@vaadin/hilla-models";
import type FormArrayTypes from "./FormArrayTypes.js";
import FormArrayTypesModel from "./FormArrayTypesModel.js";
import FormDataPrimitivesModel from "./FormDataPrimitivesModel.js";
import type FormEntity from "./FormEntity.js";
import FormEntityHierarchyModel from "./FormEntityHierarchyModel.js";
import FormEntityIdModel from "./FormEntityIdModel.js";
import FormEnumTypesModel from "./FormEnumTypesModel.js";
import FormNonnullTypesModel from "./FormNonnullTypesModel.js";
import type FormOptionalTypes from "./FormOptionalTypes.js";
import FormOptionalTypesModel from "./FormOptionalTypesModel.js";
import type FormRecordTypes from "./FormRecordTypes.js";
import FormRecordTypesModel from "./FormRecordTypesModel.js";
import FormTemporalTypesModel from "./FormTemporalTypesModel.js";
import type FormValidationConstraints from "./FormValidationConstraints.js";
import FormValidationConstraintsModel from "./FormValidationConstraintsModel.js";
const FormEntityModel = m
  .extend(FormEntityIdModel)
  .object<FormEntity>("FormEntity")
  .property("myId", NumberModel)
  .property("dataPrimitives", FormDataPrimitivesModel)
  .property("entityHierarchy", FormEntityHierarchyModel)
  .property("temporalTypes", FormTemporalTypesModel)
  .property("arrayTypes", m.lazy((): ObjectModel<FormArrayTypes> => FormArrayTypesModel))
  .property("enumTypes", FormEnumTypesModel)
  .property("recordTypes", m.lazy((): ObjectModel<FormRecordTypes> => FormRecordTypesModel))
  .property("validationConstraints", m.lazy((): ObjectModel<FormValidationConstraints> => FormValidationConstraintsModel))
  .property("myOptionalTypes", m.lazy((): ObjectModel<FormOptionalTypes> => FormOptionalTypesModel))
  .property("nonnullTypes", FormNonnullTypesModel)
  .property("unknownModel", m.optional(Model))
  .build();
type FormEntityModel = typeof FormEntityModel;
export default FormEntityModel;
