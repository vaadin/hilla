import m, { BooleanModel, Email, NotBlank, ObjectModel, StringModel } from "@vaadin/hilla-models";
import AbstractEntityModel from "./AbstractEntityModel.js";
import AddressModel from "./AddressModel.js";
import EmployeeModel from "./EmployeeModel.js";
import type Person from "./Person.js";
const PersonModel: ObjectModel<Person> = m
  .extend(AbstractEntityModel)
  .object<Person>("Person")
  .property("firstName", m.meta(m.constrained(StringModel, NotBlank()), { jvmType: "java.lang.String" }))
  .property("lastName", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("email", m.meta(m.constrained(m.optional(StringModel), Email()), { jvmType: "java.lang.String" }))
  .property("phone", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("address", m.meta(m.array(AddressModel), { annotations: [{ jvmType: "jakarta.persistence.OneToMany" }], jvmType: "java.util.List" }))
  .property("team", m.meta(m.record(m.meta(m.array(m.optional(m.lazy(() => EmployeeModel))), { jvmType: "java.util.List" })), { jvmType: "java.util.Map" }))
  .property("dateOfBirth", m.meta(m.optional(StringModel), { jvmType: "java.time.LocalDate" }))
  .property("occupation", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("role", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("important", m.meta(BooleanModel, { jvmType: "boolean" }))
  .build();
type PersonModel = typeof PersonModel;
export default PersonModel;
