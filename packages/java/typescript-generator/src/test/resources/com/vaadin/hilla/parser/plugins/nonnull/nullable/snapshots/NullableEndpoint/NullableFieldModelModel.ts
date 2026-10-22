import m, { NumberModel, StringModel } from "@vaadin/hilla-models";
import type NullableFieldModel from "./NullableFieldModel.js";
const NullableFieldModelModel = m
  .object<NullableFieldModel>("NullableFieldModel")
  .property("id", m.meta(m.optional(StringModel), { annotations: [{ jvmType: "jakarta.persistence.Id" }], jvmType: "java.lang.String" }))
  .property("version", m.meta(m.optional(NumberModel), { annotations: [{ jvmType: "jakarta.persistence.Version" }], jvmType: "java.lang.Long" }))
  .property("jakartaNonnull", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("jspecifyNonnull", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("springNonnull", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .build();
type NullableFieldModelModel = typeof NullableFieldModelModel;
export default NullableFieldModelModel;
