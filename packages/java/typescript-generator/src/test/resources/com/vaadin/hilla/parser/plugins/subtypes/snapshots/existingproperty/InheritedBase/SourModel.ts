import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import InheritedBaseModel_1 from "../InheritedBaseModel.js";
import type Sour_1 from "./Sour.js";
class SourModel<T extends Sour_1 = Sour_1> extends InheritedBaseModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SourModel);
    get level(): NumberModel_1 {
        return this[_getPropertyModel_1]("level", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default SourModel;
