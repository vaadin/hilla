import m, { NumberModel, StringModel } from "@vaadin/hilla-models";
import type FormEntityMetadata from "./FormEntityMetadata.js";
import FormEntityModel from "./FormEntityModel.js";
const FormEntityMetadataModel = m
  .object<FormEntityMetadata>("FormEntityMetadata")
  .property("withoutMetadata", StringModel)
  .property("withJavaType", m.meta(StringModel, { jvmType: "java.time.LocalDateTime" }))
  .property("listWithJavaType", m.meta(m.array(m.meta(m.optional(StringModel), { jvmType: "java.time.LocalDateTime" })), { jvmType: "java.util.List" }))
  .property("withAnnotations", m.meta(NumberModel, { annotations: [{ jvmType: "jakarta.persistence.Id" }, { jvmType: "jakarta.persistence.Version" }] }))
  .property("listWithAnnotations", m.meta(m.array(m.meta(m.optional(NumberModel), { annotations: [{ jvmType: "jakarta.persistence.Id" }, { jvmType: "jakarta.persistence.Version" }] })), { jvmType: "java.util.List" }))
  .property("withAll", m.meta(NumberModel, { annotations: [{ jvmType: "jakarta.persistence.Id" }, { jvmType: "jakarta.persistence.Version" }], jvmType: "java.lang.Long" }))
  .property("nestedModelWithAnnotations", m.meta(FormEntityModel, { annotations: [{ jvmType: "jakarta.persistence.OneToOne" }] }))
  .build();
type FormEntityMetadataModel = typeof FormEntityMetadataModel;
export default FormEntityMetadataModel;
