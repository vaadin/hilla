import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type Shape from "./Shape.js";
class ShapeModel<T extends Shape = Shape> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ShapeModel);
}
export default ShapeModel;
