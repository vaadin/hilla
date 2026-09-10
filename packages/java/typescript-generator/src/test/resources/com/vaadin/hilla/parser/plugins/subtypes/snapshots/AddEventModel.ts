import m, { StringModel } from "@vaadin/hilla-models";
import type AddEvent from "./AddEvent.js";
import BaseEventModel from "./BaseEventModel.js";
const AddEventModel = m
  .extend(BaseEventModel)
  .object<AddEvent>("AddEvent")
  .property("item", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("@type", m.literal("add"))
  .build();
type AddEventModel = typeof AddEventModel;
export default AddEventModel;
