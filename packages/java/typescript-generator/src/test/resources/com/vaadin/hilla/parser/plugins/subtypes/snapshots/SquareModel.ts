import m, { NumberModel } from "@vaadin/hilla-models";
import type Square from "./Square.js";
const SquareModel = m
  .object<Square>("Square")
  .property("side", m.meta(NumberModel, { jvmType: "double" }))
  .property("shape", m.literal("square"))
  .build();
type SquareModel = typeof SquareModel;
export default SquareModel;
