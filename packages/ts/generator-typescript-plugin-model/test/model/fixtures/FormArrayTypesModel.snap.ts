import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormArrayTypes_1 from "./FormArrayTypes.js";
import FormArrayTypesModel_1 from "./FormArrayTypesModel.js";
import FormEntityHierarchyModel_1 from "./FormEntityHierarchyModel.js";
import FormEntityModel_1 from "./FormEntityModel.js";
class FormArrayTypesModel<T extends FormArrayTypes_1 = FormArrayTypes_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormArrayTypesModel);
    get stringList(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("stringList", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new StringModel_1(parent, key, true)));
    }
    get entityHierarchyList(): ArrayModel_1<FormEntityHierarchyModel_1> {
        return this[_getPropertyModel_1]("entityHierarchyList", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new FormEntityHierarchyModel_1(parent, key, true)));
    }
    get selfReferenceList(): ArrayModel_1<FormArrayTypesModel_1> {
        return this[_getPropertyModel_1]("selfReferenceList", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new FormArrayTypesModel_1(parent, key, true)));
    }
    get stringArray(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("stringArray", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new StringModel_1(parent, key, true)));
    }
    get numberMatrix(): ArrayModel_1<ArrayModel_1<NumberModel_1>> {
        return this[_getPropertyModel_1]("numberMatrix", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new NumberModel_1(parent, key, true))));
    }
    get entityMatrix(): ArrayModel_1<ArrayModel_1<FormEntityModel_1>> {
        return this[_getPropertyModel_1]("entityMatrix", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new FormEntityModel_1(parent, key, true))));
    }
    get nestedArrays(): ArrayModel_1<ArrayModel_1<ObjectModel_1<Record<string, ReadonlyArray<string>>>>> {
        return this[_getPropertyModel_1]("nestedArrays", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ObjectModel_1(parent, key, true))));
    }
}
export default FormArrayTypesModel;
