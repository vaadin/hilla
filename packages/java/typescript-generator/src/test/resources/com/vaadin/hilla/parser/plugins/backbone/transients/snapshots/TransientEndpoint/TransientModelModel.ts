import m, { StringModel } from "@vaadin/hilla-models";
import NonTransientEntityModel from "../NonTransientEntityModel.js";
import type TransientModel from "./TransientModel.js";
const TransientModelModel = m
  .object<TransientModel>("TransientModel")
  .property("nonTransientEntity", m.optional(NonTransientEntityModel))
  .property("notTransientField", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type TransientModelModel = typeof TransientModelModel;
export default TransientModelModel;
