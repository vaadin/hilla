import m, { BooleanModel, StringModel } from "@vaadin/hilla-models";
import BaseEventModel from "./BaseEventModel.js";
import type DeleteEvent from "./DeleteEvent.js";
const DeleteEventModel = m
  .extend(BaseEventModel)
  .object<DeleteEvent>("DeleteEvent")
  .property("item", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("force", m.meta(BooleanModel, { jvmType: "boolean" }))
  .property("@type", m.literal("delete"))
  .build();
type DeleteEventModel = typeof DeleteEventModel;
export default DeleteEventModel;
