import m, { NumberModel } from "@vaadin/hilla-models";
import type AbstractEntity from "./AbstractEntity.js";
const AbstractEntityModel = m
  .object<AbstractEntity>("AbstractEntity")
  .property("version", m.meta(NumberModel, { annotations: [{ jvmType: "jakarta.persistence.Version" }], jvmType: "int" }))
  .property("id", m.meta(m.optional(NumberModel), { annotations: [{ jvmType: "jakarta.persistence.Id" }], jvmType: "java.lang.Long" }))
  .build();
type AbstractEntityModel = typeof AbstractEntityModel;
export default AbstractEntityModel;
