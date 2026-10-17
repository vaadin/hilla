import m, { NumberModel, StringModel } from "@vaadin/hilla-models";
import type User from "./User.js";
const UserModel = m
  .object<User>("User")
  .property("name", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("age", m.meta(NumberModel, { jvmType: "int" }))
  .build();
type UserModel = typeof UserModel;
export default UserModel;
