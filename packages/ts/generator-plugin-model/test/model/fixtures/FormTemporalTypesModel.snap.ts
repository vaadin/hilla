import m, { StringModel } from "@vaadin/hilla-models";
import type FormTemporalTypes from "./FormTemporalTypes.js";
const FormTemporalTypesModel = m
  .object<FormTemporalTypes>("FormTemporalTypes")
  .property("localDate", StringModel)
  .property("localTime", StringModel)
  .build();
type FormTemporalTypesModel = typeof FormTemporalTypesModel;
export default FormTemporalTypesModel;
