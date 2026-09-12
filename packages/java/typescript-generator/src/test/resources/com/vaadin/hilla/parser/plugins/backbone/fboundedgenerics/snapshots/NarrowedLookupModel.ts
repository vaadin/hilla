import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import EnumLookupValueModel_1 from "./EnumLookupValueModel.js";
import type NarrowedLookup_1 from "./NarrowedLookup.js";
class NarrowedLookupModel<T extends NarrowedLookup_1 = NarrowedLookup_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NarrowedLookupModel);
    get value(): EnumLookupValueModel_1 {
        return this[_getPropertyModel_1]("value", (parent, key) => new EnumLookupValueModel_1(parent, key, true));
    }
}
export default NarrowedLookupModel;
