import m, { BooleanModel, Email, NotBlank, StringModel } from "@vaadin/hilla-models";
import AddressModel from "../data/AddressModel.js";
import EmployeeModel from "../data/EmployeeModel.js";
import type NestedPerson from "./NestedPerson.js";
const NestedPersonModel = m
  .object<NestedPerson>("NestedPerson")
  .property("firstName", m.meta(m.constrained(StringModel, NotBlank()), { jvmType: "java.lang.String" }))
  .property("lastName", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("email", m.meta(m.constrained(m.optional(StringModel), Email()), { jvmType: "java.lang.String" }))
  .property("phone", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("address", m.meta(m.array(AddressModel), { jvmType: "java.util.List" }))
  .property("team", m.meta(m.record(m.meta(m.array(m.optional(EmployeeModel)), { jvmType: "java.util.List" })), { jvmType: "java.util.Map" }))
  .property("dateOfBirth", m.meta(m.optional(StringModel), { jvmType: "java.time.LocalDate" }))
  .property("occupation", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("role", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("important", m.meta(BooleanModel, { jvmType: "boolean" }))
  .build();
type NestedPersonModel = typeof NestedPersonModel;
export default NestedPersonModel;
