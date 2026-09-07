import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type Circle_1 from "./Circle.js";
class CircleModel<T extends Circle_1 = Circle_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CircleModel);
    get radius(): NumberModel_1 {
        return this[_getPropertyModel_1]("radius", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "double" } }));
    }
}
export default CircleModel;
