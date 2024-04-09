import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type FormNonnullTypes_1 from "./FormNonnullTypes.js";
class FormNonnullTypesModel<T extends FormNonnullTypes_1 = FormNonnullTypes_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormNonnullTypesModel);
    get nonNullableString(): StringModel_1 {
        return this[_getPropertyModel_1]("nonNullableString", (parent, key) => new StringModel_1(parent, key, false));
    }
    get nonNullableList(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("nonNullableList", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new StringModel_1(parent, key, true)));
    }
    get nonNullableMatrix(): ArrayModel_1<ArrayModel_1<StringModel_1>> {
        return this[_getPropertyModel_1]("nonNullableMatrix", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true))));
    }
}
export default FormNonnullTypesModel;
