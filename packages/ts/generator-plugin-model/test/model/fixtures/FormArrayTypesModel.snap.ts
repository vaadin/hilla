import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type FormArrayTypes from "./FormArrayTypes.js";
import FormArrayTypesModel_1 from "./FormArrayTypesModel.js";
import FormEntityHierarchyModel from "./FormEntityHierarchyModel.js";
import FormEntityModel from "./FormEntityModel.js";
class FormArrayTypesModel<T extends FormArrayTypes = FormArrayTypes> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormArrayTypesModel);
    get stringList(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("stringList", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, true)));
    }
    get entityHierarchyList(): ArrayModel<FormEntityHierarchyModel> {
        return this[_getPropertyModel]("entityHierarchyList", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new FormEntityHierarchyModel(parent, key, true)));
    }
    get selfReferenceList(): ArrayModel<FormArrayTypesModel_1> {
        return this[_getPropertyModel]("selfReferenceList", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new FormArrayTypesModel_1(parent, key, true)));
    }
    get stringArray(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("stringArray", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, true)));
    }
    get numberMatrix(): ArrayModel<ArrayModel<NumberModel>> {
        return this[_getPropertyModel]("numberMatrix", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new NumberModel(parent, key, true))));
    }
    get entityMatrix(): ArrayModel<ArrayModel<FormEntityModel>> {
        return this[_getPropertyModel]("entityMatrix", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new FormEntityModel(parent, key, true))));
    }
    get nestedArrays(): ArrayModel<ArrayModel<ObjectModel<Record<string, ReadonlyArray<string | undefined> | undefined>>>> {
        return this[_getPropertyModel]("nestedArrays", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new ObjectModel(parent, key, true))));
    }
}
export default FormArrayTypesModel;
