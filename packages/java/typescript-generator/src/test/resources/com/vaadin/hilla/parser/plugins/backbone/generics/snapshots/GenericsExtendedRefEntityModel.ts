import m from "@vaadin/hilla-models";
import GenericsBareRefEntityModel from "./GenericsBareRefEntityModel.js";
import type GenericsExtendedRefEntity from "./GenericsExtendedRefEntity.js";
const GenericsExtendedRefEntityModel = m
  .object<GenericsExtendedRefEntity>("GenericsExtendedRefEntity")
  .property("extendedGenericTypeReference", m.optional(GenericsBareRefEntityModel))
  .build();
type GenericsExtendedRefEntityModel = typeof GenericsExtendedRefEntityModel;
export default GenericsExtendedRefEntityModel;
