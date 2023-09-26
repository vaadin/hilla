import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormTemporalTypes_1 from "./FormTemporalTypes.js";
class FormTemporalTypesModel<T extends FormTemporalTypes_1 = FormTemporalTypes_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormTemporalTypesModel);
    get localDate(): StringModel_1 {
        return this[_getPropertyModel_1]("localDate", (parent, key) => new StringModel_1(parent, key, false));
    }
    get localTime(): StringModel_1 {
        return this[_getPropertyModel_1]("localTime", (parent, key) => new StringModel_1(parent, key, false));
    }
}
export default FormTemporalTypesModel;
