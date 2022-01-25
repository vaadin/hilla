import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormArrayTypes_1 from "./FormArrayTypes";
import FormArrayTypesModel_1 from "./FormArrayTypesModel";
import type FormEntity_1 from "./FormEntity";
import type FormEntityHierarchy_1 from "./FormEntityHierarchy";
import FormEntityHierarchyModel_1 from "./FormEntityHierarchyModel";
import FormEntityModel_1 from "./FormEntityModel";
class FormArrayTypesModel<T extends FormArrayTypes_1 = FormArrayTypes_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => FormArrayTypes_1;
    get stringList(): ArrayModel_1<string, StringModel_1> {
        return this[_getPropertyModel_1]("stringList", ArrayModel_1, [true, StringModel_1, [true]]) as ArrayModel_1<string, StringModel_1>;
    }
    get entityHierarchyList(): ArrayModel_1<FormEntityHierarchy_1, FormEntityHierarchyModel_1> {
        return this[_getPropertyModel_1]("entityHierarchyList", ArrayModel_1, [true, FormEntityHierarchyModel_1, [true]]) as ArrayModel_1<FormEntityHierarchy_1, FormEntityHierarchyModel_1>;
    }
    get selfReferenceList(): ArrayModel_1<FormArrayTypes_1, FormArrayTypesModel_1> {
        return this[_getPropertyModel_1]("selfReferenceList", ArrayModel_1, [true, FormArrayTypesModel_1, [true]]) as ArrayModel_1<FormArrayTypes_1, FormArrayTypesModel_1>;
    }
    get stringArray(): ArrayModel_1<string, StringModel_1> {
        return this[_getPropertyModel_1]("stringArray", ArrayModel_1, [true, StringModel_1, [true]]) as ArrayModel_1<string, StringModel_1>;
    }
    get numberMatrix(): ArrayModel_1<ReadonlyArray<number>, ArrayModel_1<number, NumberModel_1>> {
        return this[_getPropertyModel_1]("numberMatrix", ArrayModel_1, [true, ArrayModel_1, [true, NumberModel_1, [true]]]) as ArrayModel_1<ReadonlyArray<number>, ArrayModel_1<number, NumberModel_1>>;
    }
    get entityMatrix(): ArrayModel_1<ReadonlyArray<FormEntity_1>, ArrayModel_1<FormEntity_1, FormEntityModel_1>> {
        return this[_getPropertyModel_1]("entityMatrix", ArrayModel_1, [true, ArrayModel_1, [true, FormEntityModel_1, [true]]]) as ArrayModel_1<ReadonlyArray<FormEntity_1>, ArrayModel_1<FormEntity_1, FormEntityModel_1>>;
    }
    get nestedArrays(): ArrayModel_1<ReadonlyArray<Record<string, ReadonlyArray<string>>>, ArrayModel_1<Record<string, ReadonlyArray<string>>, ObjectModel_1<Record<string, ReadonlyArray<string>>>>> {
        return this[_getPropertyModel_1]("nestedArrays", ArrayModel_1, [true, ArrayModel_1, [true, ObjectModel_1, [true]]]) as ArrayModel_1<ReadonlyArray<Record<string, ReadonlyArray<string>>>, ArrayModel_1<Record<string, ReadonlyArray<string>>, ObjectModel_1<Record<string, ReadonlyArray<string>>>>>;
    }
}
export default FormArrayTypesModel;
