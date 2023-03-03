import { _getPropertyModel as _getPropertyModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormTemporalTypes_1 from "./FormTemporalTypes";
class FormTemporalTypesModel<T extends FormTemporalTypes_1 = FormTemporalTypes_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => FormTemporalTypes_1;
    get localDate(): StringModel_1 {
        return this[_getPropertyModel_1]("localDate", StringModel_1, [false]) as StringModel_1;
    }
    get localTime(): StringModel_1 {
        return this[_getPropertyModel_1]("localTime", StringModel_1, [false]) as StringModel_1;
    }
}
export default FormTemporalTypesModel;
