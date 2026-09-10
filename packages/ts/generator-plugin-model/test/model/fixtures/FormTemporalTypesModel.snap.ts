import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type FormTemporalTypes from "./FormTemporalTypes.js";
class FormTemporalTypesModel<T extends FormTemporalTypes = FormTemporalTypes> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormTemporalTypesModel);
    get localDate(): StringModel {
        return this[_getPropertyModel]("localDate", (parent, key) => new StringModel(parent, key, false));
    }
    get localTime(): StringModel {
        return this[_getPropertyModel]("localTime", (parent, key) => new StringModel(parent, key, false));
    }
}
export default FormTemporalTypesModel;
