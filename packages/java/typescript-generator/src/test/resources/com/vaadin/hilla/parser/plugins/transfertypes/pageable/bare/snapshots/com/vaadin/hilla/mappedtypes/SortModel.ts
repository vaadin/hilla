import m from "@vaadin/hilla-models";
import OrderModel from "./OrderModel.js";
import type Sort from "./Sort.js";
const SortModel = m
  .object<Sort>("Sort")
  .property("orders", m.meta(m.array(m.optional(OrderModel)), { jvmType: "java.util.List" }))
  .build();
type SortModel = typeof SortModel;
export default SortModel;
