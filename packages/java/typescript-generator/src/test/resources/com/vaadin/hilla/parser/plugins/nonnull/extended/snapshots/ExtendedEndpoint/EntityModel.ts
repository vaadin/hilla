import m, { StringModel } from "@vaadin/hilla-models";
import type Entity from "./Entity.js";
const EntityModel = m
  .object<Entity>("Entity")
  .property("nonnullListOfNullableStrings", m.meta(m.array(m.meta(m.optional(StringModel), { jvmType: "java.lang.String" })), { jvmType: "java.util.List" }))
  .build();
type EntityModel = typeof EntityModel;
export default EntityModel;
