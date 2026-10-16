import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type InheritedEnum_1 from "./InheritedEnum.js";
import ColourModel_1 from "./InheritedEnum/ColourModel.js";
class InheritedEnumModel<T extends InheritedEnum_1 = InheritedEnum_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(InheritedEnumModel);
    get colour(): ColourModel_1 {
        return this[_getPropertyModel_1]("colour", (parent, key) => new ColourModel_1(parent, key, true));
    }
}
export default InheritedEnumModel;
