import m, { StringModel } from "@vaadin/hilla-models";
import type FormNonnullTypes from "./FormNonnullTypes.js";
const FormNonnullTypesModel = m
  .object<FormNonnullTypes>("FormNonnullTypes")
  .property("nonNullableString", StringModel)
  .property("nonNullableList", m.array(m.optional(StringModel)))
  .property("nonNullableMatrix", m.array(m.optional(m.array(m.optional(StringModel)))))
  .build();
type FormNonnullTypesModel = typeof FormNonnullTypesModel;
export default FormNonnullTypesModel;
