import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import DeclaredBaseModel_1 from "../DeclaredBaseModel.js";
import type Second_1 from "./Second.js";
class SecondModel<T extends Second_1 = Second_1> extends DeclaredBaseModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SecondModel);
    get size(): NumberModel_1 {
        return this[_getPropertyModel_1]("size", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default SecondModel;
