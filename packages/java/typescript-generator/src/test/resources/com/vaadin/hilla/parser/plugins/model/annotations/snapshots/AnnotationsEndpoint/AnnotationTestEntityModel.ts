import m, { NumberModel, StringModel } from "@vaadin/hilla-models";
import type AnnotationTestEntity from "./AnnotationTestEntity.js";
import NestedEntityModel from "./NestedEntityModel.js";
const AnnotationTestEntityModel = m
  .object<AnnotationTestEntity>("AnnotationTestEntity")
  .property("id", m.meta(m.optional(NumberModel), { annotations: [{ jvmType: "jakarta.persistence.Id" }], jvmType: "java.lang.Long" }))
  .property("version", m.meta(m.optional(NumberModel), { annotations: [{ jvmType: "jakarta.persistence.Version" }], jvmType: "int" }))
  .property("oneToOne", m.meta(m.optional(NestedEntityModel), { annotations: [{ jvmType: "jakarta.persistence.OneToOne" }] }))
  .property("oneToMany", m.meta(m.optional(m.array(m.optional(NestedEntityModel))), { annotations: [{ jvmType: "jakarta.persistence.OneToMany" }], jvmType: "java.util.List" }))
  .property("manyToOne", m.meta(m.optional(NestedEntityModel), { annotations: [{ jvmType: "jakarta.persistence.ManyToOne" }] }))
  .property("manyToMany", m.meta(m.optional(m.array(m.optional(NestedEntityModel))), { annotations: [{ jvmType: "jakarta.persistence.ManyToMany" }], jvmType: "java.util.List" }))
  .property("manyToManyWithFetchType", m.meta(m.optional(m.array(m.optional(NestedEntityModel))), { annotations: [{ jvmType: "jakarta.persistence.ManyToMany", attributes: { fetch: "EAGER" } }], jvmType: "java.util.List" }))
  .property("name", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type AnnotationTestEntityModel = typeof AnnotationTestEntityModel;
export default AnnotationTestEntityModel;
