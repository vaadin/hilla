import m, { StringModel } from "@vaadin/hilla-models";
import type GenericsBareEntity from "./GenericsBareEntity.js";
const GenericsBareEntityModel = m
  .object<GenericsBareEntity>("GenericsBareEntity")
  .property("bareEntityProperty", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type GenericsBareEntityModel = typeof GenericsBareEntityModel;
export default GenericsBareEntityModel;
