import m, { Model } from "@vaadin/hilla-models";
import type Generic from "./Generic.js";
const GenericModel = m
  .object<Generic>("Generic")
  .property("genericField", Model)
  .build();
type GenericModel = typeof GenericModel;
export default GenericModel;
