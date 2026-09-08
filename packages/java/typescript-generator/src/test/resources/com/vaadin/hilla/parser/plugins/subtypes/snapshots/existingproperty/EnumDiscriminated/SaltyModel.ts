import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import EnumDiscriminatedModel_1 from "../EnumDiscriminatedModel.js";
import type Salty_1 from "./Salty.js";
class SaltyModel<T extends Salty_1 = Salty_1> extends EnumDiscriminatedModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SaltyModel);
    get level(): NumberModel_1 {
        return this[_getPropertyModel_1]("level", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default SaltyModel;
