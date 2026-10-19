import m, { BooleanModel, NotBlank, StringModel } from "@vaadin/hilla-models";
import DirectionModel from "../../../../org/springframework/data/domain/Sort/DirectionModel.js";
import NullHandlingModel from "../../../../org/springframework/data/domain/Sort/NullHandlingModel.js";
import type Order from "./Order.js";
const OrderModel = m
  .object<Order>("Order")
  .property("direction", DirectionModel)
  .property("property", m.meta(m.constrained(StringModel, NotBlank()), { jvmType: "java.lang.String" }))
  .property("ignoreCase", m.meta(BooleanModel, { jvmType: "boolean" }))
  .property("nullHandling", m.optional(NullHandlingModel))
  .build();
type OrderModel = typeof OrderModel;
export default OrderModel;
