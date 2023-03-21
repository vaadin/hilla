import { _getPropertyModel as _getPropertyModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import type FormEntityHierarchy_1 from "./FormEntityHierarchy.js";
import type FormOptionalTypes_1 from "./FormOptionalTypes.js";
import type FormRecordTypes_1 from "./FormRecordTypes.js";
class FormRecordTypesModel<T extends FormRecordTypes_1 = FormRecordTypes_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => FormRecordTypes_1;
    get stringMap(): ObjectModel_1<Record<string, string>> {
        return this[_getPropertyModel_1]("stringMap", ObjectModel_1, [false]) as ObjectModel_1<Record<string, string>>;
    }
    get entityHierarchyMap(): ObjectModel_1<Record<string, FormEntityHierarchy_1>> {
        return this[_getPropertyModel_1]("entityHierarchyMap", ObjectModel_1, [false]) as ObjectModel_1<Record<string, FormEntityHierarchy_1>>;
    }
    get stringListMap(): ObjectModel_1<Record<string, ReadonlyArray<string>>> {
        return this[_getPropertyModel_1]("stringListMap", ObjectModel_1, [false]) as ObjectModel_1<Record<string, ReadonlyArray<string>>>;
    }
    get selfReferenceMap(): ObjectModel_1<Record<string, FormRecordTypes_1>> {
        return this[_getPropertyModel_1]("selfReferenceMap", ObjectModel_1, [false]) as ObjectModel_1<Record<string, FormRecordTypes_1>>;
    }
    get complexMap(): ObjectModel_1<Record<string, Record<string, ReadonlyArray<FormOptionalTypes_1>>>> {
        return this[_getPropertyModel_1]("complexMap", ObjectModel_1, [false]) as ObjectModel_1<Record<string, Record<string, ReadonlyArray<FormOptionalTypes_1>>>>;
    }
    get objectMap(): ObjectModel_1<Record<string, unknown>> {
        return this[_getPropertyModel_1]("objectMap", ObjectModel_1, [false]) as ObjectModel_1<Record<string, unknown>>;
    }
}
export default FormRecordTypesModel;
