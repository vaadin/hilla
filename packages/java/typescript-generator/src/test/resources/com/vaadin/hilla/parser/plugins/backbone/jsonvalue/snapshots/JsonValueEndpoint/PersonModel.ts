import m, { NumberModel, StringModel } from "@vaadin/hilla-models";
import type Person from "./Person.js";
const PersonModel = m
  .object<Person>("Person")
  .property("name", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("email", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("phoneNumber", m.meta(NumberModel, { jvmType: "int" }))
  .build();
type PersonModel = typeof PersonModel;
export default PersonModel;
