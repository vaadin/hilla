import m, { Email, Min, NotBlank, NumberModel, Pattern, Size, StringModel } from "@vaadin/hilla-models";
import type Validated from "./Validated.js";
const ValidatedModel = m
  .object<Validated>("Validated")
  .property("id", m.meta(m.optional(NumberModel), { annotations: [{ jvmType: "jakarta.persistence.Id" }], jvmType: "long" }))
  .property("name", m.meta(m.constrained(m.optional(StringModel), NotBlank()), { jvmType: "java.lang.String" }))
  .property("count", m.meta(m.constrained(NumberModel, Min(1)), { jvmType: "int" }))
  .property("address", m.meta(m.constrained(m.optional(StringModel), Email({ message: "that isn't an address" })), { jvmType: "java.lang.String" }))
  .property("code", m.meta(m.constrained(m.optional(StringModel), Pattern({ regexp: "[a-z]+" })), { jvmType: "java.lang.String" }))
  .property("codes", m.meta(m.optional(m.array(m.meta(m.constrained(m.optional(StringModel), Size({ min: 1 })), { jvmType: "java.lang.String" }))), { jvmType: "java.lang.String[]" }))
  .property("tags", m.meta(m.constrained(m.optional(m.array(m.meta(m.constrained(m.optional(StringModel), NotBlank()), { jvmType: "java.lang.String" }))), Size({ min: 1, max: 10 })), { jvmType: "java.util.List" }))
  .build();
type ValidatedModel = typeof ValidatedModel;
export default ValidatedModel;
