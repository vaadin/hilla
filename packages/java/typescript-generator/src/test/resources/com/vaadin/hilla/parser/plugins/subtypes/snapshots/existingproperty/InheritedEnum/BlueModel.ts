import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import InheritedEnumModel_1 from "../InheritedEnumModel.js";
import type Blue_1 from "./Blue.js";
class BlueModel<T extends Blue_1 = Blue_1> extends InheritedEnumModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(BlueModel);
    get level(): NumberModel_1 {
        return this[_getPropertyModel_1]("level", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default BlueModel;
