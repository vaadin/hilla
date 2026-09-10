import m, { Model, StringModel } from "@vaadin/hilla-models";
import type Collection from "./Collection.js";
const CollectionModel = m
  .object<Collection>("Collection")
  .property("items", m.meta(m.optional(m.array(m.optional(Model))), { jvmType: "java.util.List" }))
  .property("name", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type CollectionModel = typeof CollectionModel;
export default CollectionModel;
