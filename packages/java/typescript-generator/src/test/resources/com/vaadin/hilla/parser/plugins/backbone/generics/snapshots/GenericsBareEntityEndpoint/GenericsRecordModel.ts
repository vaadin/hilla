import m, { Model } from "@vaadin/hilla-models";
import type GenericsRecord from "./GenericsRecord.js";
const GenericsRecordModel = m
  .object<GenericsRecord>("GenericsRecord")
  .property("first", m.optional(Model))
  .property("second", m.optional(Model))
  .build();
type GenericsRecordModel = typeof GenericsRecordModel;
export default GenericsRecordModel;
