import m, { StringModel } from "@vaadin/hilla-models";
import type NonNullableFieldModel from "./NonNullableFieldModel.js";
const NonNullableFieldModelModel = m
  .object<NonNullableFieldModel>("NonNullableFieldModel")
  .property("stringList", m.meta(m.array(m.meta(StringModel, { jvmType: "java.lang.String" })), { jvmType: "java.util.List" }))
  .build();
type NonNullableFieldModelModel = typeof NonNullableFieldModelModel;
export default NonNullableFieldModelModel;
