import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type EnumLookupValue_1 from "./EnumLookupValue.js";
class EnumLookupValueModel<T extends EnumLookupValue_1 = EnumLookupValue_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(EnumLookupValueModel);
    get key(): ObjectModel_1 {
        return this[_getPropertyModel_1]("key", (parent, key) => new ObjectModel_1(parent, key, true));
    }
    get label(): StringModel_1 {
        return this[_getPropertyModel_1]("label", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default EnumLookupValueModel;
