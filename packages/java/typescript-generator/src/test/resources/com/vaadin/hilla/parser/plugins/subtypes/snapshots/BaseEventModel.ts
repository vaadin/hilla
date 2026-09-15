import m, { NumberModel } from "@vaadin/hilla-models";
import type BaseEvent from "./BaseEvent.js";
const BaseEventModel = m
  .object<BaseEvent>("BaseEvent")
  .property("id", m.meta(NumberModel, { jvmType: "int" }))
  .build();
type BaseEventModel = typeof BaseEventModel;
export default BaseEventModel;
