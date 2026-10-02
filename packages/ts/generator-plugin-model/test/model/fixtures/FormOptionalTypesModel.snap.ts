import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import FormEntityModel from "./FormEntityModel.js";
import type FormOptionalTypes from "./FormOptionalTypes.js";
class FormOptionalTypesModel<T extends FormOptionalTypes = FormOptionalTypes> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormOptionalTypesModel);
    get optionalString(): StringModel {
        return this[_getPropertyModel]("optionalString", (parent, key) => new StringModel(parent, key, false));
    }
    get optionalEntity(): FormEntityModel {
        return this[_getPropertyModel]("optionalEntity", (parent, key) => new FormEntityModel(parent, key, false));
    }
    get optionalList(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("optionalList", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, true)));
    }
    get optionalMatrix(): ArrayModel<ArrayModel<StringModel>> {
        return this[_getPropertyModel]("optionalMatrix", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true))));
    }
}
export default FormOptionalTypesModel;
