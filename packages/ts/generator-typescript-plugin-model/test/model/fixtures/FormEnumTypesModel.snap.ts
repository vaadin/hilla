import { _getPropertyModel as _getPropertyModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormEnumTypes_1 from "./FormEnumTypes";
class FormEnumTypesModel<T extends FormEnumTypes_1 = FormEnumTypes_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => FormEnumTypes_1;
    get enumProperty(): StringModel_1 {
        return this[_getPropertyModel_1]("enumProperty", StringModel_1, [true]) as StringModel_1;
    }
}
export default FormEnumTypesModel;
