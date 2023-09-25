import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import FormEntityModel_1 from "./FormEntityModel.js";
import type FormOptionalTypes_1 from "./FormOptionalTypes.js";
class FormOptionalTypesModel<T extends FormOptionalTypes_1 = FormOptionalTypes_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormOptionalTypesModel);
    get optionalString(): StringModel_1 {
        return this[_getPropertyModel_1]("optionalString", (parent, key) => new StringModel_1(parent, key, false));
    }
    get optionalEntity(): FormEntityModel_1 {
        return this[_getPropertyModel_1]("optionalEntity", (parent, key) => new FormEntityModel_1(parent, key, false));
    }
    get optionalList(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("optionalList", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new StringModel_1(parent, key, true)));
    }
    get optionalMatrix(): ArrayModel_1<ArrayModel_1<StringModel_1>> {
        return this[_getPropertyModel_1]("optionalMatrix", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true))));
    }
}
export default FormOptionalTypesModel;
