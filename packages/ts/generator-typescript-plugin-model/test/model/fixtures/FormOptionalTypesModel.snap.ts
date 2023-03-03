import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import FormEntityModel_1 from "./FormEntityModel";
import type FormOptionalTypes_1 from "./FormOptionalTypes";
class FormOptionalTypesModel<T extends FormOptionalTypes_1 = FormOptionalTypes_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => FormOptionalTypes_1;
    get optionalString(): StringModel_1 {
        return this[_getPropertyModel_1]("optionalString", StringModel_1, [false]) as StringModel_1;
    }
    get optionalEntity(): FormEntityModel_1 {
        return this[_getPropertyModel_1]("optionalEntity", FormEntityModel_1, [false]) as FormEntityModel_1;
    }
    get optionalList(): ArrayModel_1<string, StringModel_1> {
        return this[_getPropertyModel_1]("optionalList", ArrayModel_1, [false, StringModel_1, [true]]) as ArrayModel_1<string, StringModel_1>;
    }
    get optionalMatrix(): ArrayModel_1<ReadonlyArray<string>, ArrayModel_1<string, StringModel_1>> {
        return this[_getPropertyModel_1]("optionalMatrix", ArrayModel_1, [false, ArrayModel_1, [true, StringModel_1, [true]]]) as ArrayModel_1<ReadonlyArray<string>, ArrayModel_1<string, StringModel_1>>;
    }
}
export default FormOptionalTypesModel;
