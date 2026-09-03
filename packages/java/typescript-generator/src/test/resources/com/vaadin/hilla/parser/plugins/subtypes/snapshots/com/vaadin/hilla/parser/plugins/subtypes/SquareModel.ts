import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type Square_1 from "./Square.js";
class SquareModel<T extends Square_1 = Square_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SquareModel);
    get side(): NumberModel_1 {
        return this[_getPropertyModel_1]("side", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "double" } }));
    }
}
export default SquareModel;
