import m, { StringModel } from "@vaadin/hilla-models";
import type Collection from "./Collection.js";
const CollectionModel = m
  .object<Collection>("Collection")
  .property("author", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("collectionName", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("type", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type CollectionModel = typeof CollectionModel;
export default CollectionModel;
