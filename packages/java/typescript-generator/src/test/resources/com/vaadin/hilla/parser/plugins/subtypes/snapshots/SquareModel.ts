import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import type Square from "./Square.js";
class SquareModel<T extends Square = Square> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(SquareModel);
    get side(): NumberModel {
        return this[_getPropertyModel]("side", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "double" } }));
    }
}
export default SquareModel;
