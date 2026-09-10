import m, { StringModel } from "@vaadin/hilla-models";
import type ExtendedNonNullableModel from "./ExtendedNonNullableModel.js";
import NonNullableModelModel from "./NonNullableModelModel.js";
const ExtendedNonNullableModelModel = m
  .extend(NonNullableModelModel)
  .object<ExtendedNonNullableModel>("ExtendedNonNullableModel")
  .property("mixedAnnotations", m.meta(m.array(m.meta(StringModel, { jvmType: "java.lang.String" })), { jvmType: "java.util.List" }))
  .property("nonTypeAnnotation", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .build();
type ExtendedNonNullableModelModel = typeof ExtendedNonNullableModelModel;
export default ExtendedNonNullableModelModel;
