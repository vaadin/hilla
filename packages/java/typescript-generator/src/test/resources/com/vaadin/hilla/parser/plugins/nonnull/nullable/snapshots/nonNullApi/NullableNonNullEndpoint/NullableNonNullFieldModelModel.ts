import m, { NumberModel, StringModel } from "@vaadin/hilla-models";
import type NullableNonNullFieldModel from "./NullableNonNullFieldModel.js";
const NullableNonNullFieldModelModel = m
  .object<NullableNonNullFieldModel>("NullableNonNullFieldModel")
  .property("required", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("id", m.meta(m.optional(StringModel), { annotations: [{ jvmType: "jakarta.persistence.Id" }], jvmType: "java.lang.String" }))
  .property("version", m.meta(m.optional(NumberModel), { annotations: [{ jvmType: "jakarta.persistence.Version" }], jvmType: "java.lang.Long" }))
  .property("notNullVersion", m.meta(NumberModel, { annotations: [{ jvmType: "jakarta.persistence.Version" }], jvmType: "java.lang.Long" }))
  .property("jakartaNullable", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("jspecifyNullable", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("springNullable", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type NullableNonNullFieldModelModel = typeof NullableNonNullFieldModelModel;
export default NullableNonNullFieldModelModel;
