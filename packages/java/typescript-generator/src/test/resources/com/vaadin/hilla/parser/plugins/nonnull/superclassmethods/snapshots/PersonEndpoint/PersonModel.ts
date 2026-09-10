import m, { StringModel } from "@vaadin/hilla-models";
import type Person from "./Person.js";
const PersonModel = m
  .object<Person>("Person")
  .property("name", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type PersonModel = typeof PersonModel;
export default PersonModel;
