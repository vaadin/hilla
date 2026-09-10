import m from "@vaadin/hilla-models";
import type Shape from "./Shape.js";
const ShapeModel = m.object<Shape>("Shape").build();
type ShapeModel = typeof ShapeModel;
export default ShapeModel;
