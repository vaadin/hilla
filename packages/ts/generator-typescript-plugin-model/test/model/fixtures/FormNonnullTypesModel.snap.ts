import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/form";
import type FormNonnullTypes_1 from "./FormNonnullTypes";
class FormNonnullTypesModel<T extends FormNonnullTypes_1 = FormNonnullTypes_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => FormNonnullTypes_1;
    get nonNullableString(): StringModel_1 {
        return this[_getPropertyModel_1]("nonNullableString", StringModel_1, [false]) as StringModel_1;
    }
    get nonNullableList(): ArrayModel_1<string, StringModel_1> {
        return this[_getPropertyModel_1]("nonNullableList", ArrayModel_1, [false, StringModel_1, [true]]) as ArrayModel_1<string, StringModel_1>;
    }
    get nonNullableMatrix(): ArrayModel_1<ReadonlyArray<string>, ArrayModel_1<string, StringModel_1>> {
        return this[_getPropertyModel_1]("nonNullableMatrix", ArrayModel_1, [false, ArrayModel_1, [true, StringModel_1, [true]]]) as ArrayModel_1<ReadonlyArray<string>, ArrayModel_1<string, StringModel_1>>;
    }
}
export default FormNonnullTypesModel;
