import m, { StringModel } from "@vaadin/hilla-models";
import BaseEventModel from "./BaseEventModel.js";
import type UpdateEvent from "./UpdateEvent.js";
const UpdateEventModel = m
  .extend(BaseEventModel)
  .object<UpdateEvent>("UpdateEvent")
  .property("oldItem", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("newItem", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("@type", m.literal("update"))
  .build();
type UpdateEventModel = typeof UpdateEventModel;
export default UpdateEventModel;
