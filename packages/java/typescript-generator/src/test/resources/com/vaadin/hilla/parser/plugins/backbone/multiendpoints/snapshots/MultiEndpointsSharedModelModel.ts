import m, { StringModel } from "@vaadin/hilla-models";
import type MultiEndpointsSharedModel from "./MultiEndpointsSharedModel.js";
const MultiEndpointsSharedModelModel = m
  .object<MultiEndpointsSharedModel>("MultiEndpointsSharedModel")
  .property("id", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type MultiEndpointsSharedModelModel = typeof MultiEndpointsSharedModelModel;
export default MultiEndpointsSharedModelModel;
