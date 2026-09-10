import m, { StringModel } from "@vaadin/hilla-models";
import AbstractEntityModel from "./AbstractEntityModel.js";
import type Address from "./Address.js";
const AddressModel = m
  .extend(AbstractEntityModel)
  .object<Address>("Address")
  .property("street", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("zipCode", m.meta(StringModel, { jvmType: "java.lang.String" }))
  .property("city", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type AddressModel = typeof AddressModel;
export default AddressModel;
