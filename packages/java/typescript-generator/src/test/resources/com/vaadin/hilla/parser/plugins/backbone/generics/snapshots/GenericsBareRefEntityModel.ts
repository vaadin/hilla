import m, { Model } from "@vaadin/hilla-models";
import type GenericsBareRefEntity from "./GenericsBareRefEntity.js";
const GenericsBareRefEntityModel = m
  .object<GenericsBareRefEntity>("GenericsBareRefEntity")
  .property("bareGenericTypeProperty", m.optional(Model))
  .property("bareRefEntityProperty", m.optional(m.self))
  .build();
type GenericsBareRefEntityModel = typeof GenericsBareRefEntityModel;
export default GenericsBareRefEntityModel;
