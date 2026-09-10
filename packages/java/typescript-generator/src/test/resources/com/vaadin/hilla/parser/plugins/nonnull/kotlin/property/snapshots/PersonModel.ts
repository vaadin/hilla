import m, { BooleanModel, Email, NotBlank, NumberModel, StringModel } from "@vaadin/hilla-models";
import AbstractEntityModel from "./AbstractEntityModel.js";
import AddressModel from "./AddressModel.js";
import type Person from "./Person.js";
const PersonModel = m
  .extend(AbstractEntityModel)
  .object<Person>("Person")
  .property("id", m.meta(m.optional(NumberModel), { jvmType: "java.lang.Long" }))
  .property("firstName", m.meta(m.constrained(StringModel, NotBlank()), { jvmType: "java.lang.String" }))
  .property("lastName", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("email", m.meta(m.constrained(m.optional(StringModel), Email()), { jvmType: "java.lang.String" }))
  .property("phone", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("important", m.meta(BooleanModel, { jvmType: "boolean" }))
  .property("luckyNumber", m.meta(NumberModel, { jvmType: "int" }))
  .property("addresses", m.meta(m.record(AddressModel), { jvmType: "java.util.Map" }))
  .property("profilePicture", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("age", m.meta(m.optional(NumberModel), { jvmType: "java.lang.Integer" }))
  .property("fullName", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .build();
type PersonModel = typeof PersonModel;
export default PersonModel;
