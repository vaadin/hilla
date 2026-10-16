import m, { Model, NumberModel, StringModel } from "@vaadin/hilla-models";
import type Nested from "./Nested.js";
const NestedModel = m
  .object<Nested>("Nested")
  .property("numberMatrix", m.meta(m.optional(m.array(m.meta(m.optional(m.array(m.meta(m.optional(NumberModel), { jvmType: "java.lang.Integer" }))), { jvmType: "java.util.List" }))), { jvmType: "java.util.List" }))
  .property("entityMatrix", m.meta(m.optional(m.array(m.meta(m.optional(m.array(m.optional(m.self))), { jvmType: "java.util.List" }))), { jvmType: "java.util.List" }))
  .property("stringMatrix", m.meta(m.optional(m.array(m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))), { jvmType: "java.lang.String[]" }))), { jvmType: "java.lang.String[][]" }))
  .property("objectMap", m.meta(m.optional(m.record(Model)), { jvmType: "java.util.Map" }))
  .build();
type NestedModel = typeof NestedModel;
export default NestedModel;
