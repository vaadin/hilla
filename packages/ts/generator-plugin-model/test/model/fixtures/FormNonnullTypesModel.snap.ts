import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type FormNonnullTypes from "./FormNonnullTypes.js";
class FormNonnullTypesModel<T extends FormNonnullTypes = FormNonnullTypes> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormNonnullTypesModel);
    get nonNullableString(): StringModel {
        return this[_getPropertyModel]("nonNullableString", (parent, key) => new StringModel(parent, key, false));
    }
    get nonNullableList(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("nonNullableList", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, true)));
    }
    get nonNullableMatrix(): ArrayModel<ArrayModel<StringModel>> {
        return this[_getPropertyModel]("nonNullableMatrix", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true))));
    }
}
export default FormNonnullTypesModel;
