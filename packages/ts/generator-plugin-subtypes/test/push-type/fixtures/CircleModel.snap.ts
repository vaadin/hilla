import m, { NumberModel } from "@vaadin/hilla-models";
import type Circle from "./Circle.js";
const CircleModel = m
  .object<Circle>("Circle")
  .property("radius", NumberModel)
  .property("shape", m.literal("circle"))
  .build();
type CircleModel = typeof CircleModel;
export default CircleModel;
