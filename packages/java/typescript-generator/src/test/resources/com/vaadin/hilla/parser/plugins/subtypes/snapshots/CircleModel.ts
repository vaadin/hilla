import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import type Circle from "./Circle.js";
class CircleModel<T extends Circle = Circle> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(CircleModel);
    get radius(): NumberModel {
        return this[_getPropertyModel]("radius", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "double" } }));
    }
}
export default CircleModel;
