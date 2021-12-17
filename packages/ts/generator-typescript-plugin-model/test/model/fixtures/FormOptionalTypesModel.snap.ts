import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/form";
import type FormEntity_1 from "./FormEntity";
import FormEntityModel_1 from "./FormEntityModel";
import type FormOptionalTypes_1 from "./FormOptionalTypes";
export default class FormOptionalTypesModel<T extends FormOptionalTypes_1 = FormOptionalTypes_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => FormOptionalTypes_1;
    get optionalString(): StringModel_1 {
        return this[_getPropertyModel_1]("optionalString", StringModel_1, [true]) as StringModel_1;
    }
    get optionalEntity(): FormEntityModel_1 {
        return this[_getPropertyModel_1]("optionalEntity", FormEntityModel_1, [true]) as FormEntityModel_1;
    }
    get optionalList(): ArrayModel_1<string, StringModel_1> {
        return this[_getPropertyModel_1]("optionalList", ArrayModel_1, [true, StringModel_1, [true]]) as ArrayModel_1<string, StringModel_1>;
    }
    get optionalMatrix(): ArrayModel_1<ReadonlyArray<string>, ArrayModel_1<string, StringModel_1>> {
        return this[_getPropertyModel_1]("optionalMatrix", ArrayModel_1, [true, ArrayModel_1, [true, StringModel_1, [true]]]) as ArrayModel_1<ReadonlyArray<string>, ArrayModel_1<string, StringModel_1>>;
    }
}
