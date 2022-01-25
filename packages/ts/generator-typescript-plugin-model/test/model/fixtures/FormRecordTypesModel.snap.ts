import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormEntityHierarchy_1 from "./FormEntityHierarchy";
import FormEntityHierarchyModel_1 from "./FormEntityHierarchyModel";
import type FormOptionalTypes_1 from "./FormOptionalTypes";
import FormOptionalTypesModel_1 from "./FormOptionalTypesModel";
import type FormRecordTypes_1 from "./FormRecordTypes";
import FormRecordTypesModel_1 from "./FormRecordTypesModel";
class FormRecordTypesModel<T extends FormRecordTypes_1 = FormRecordTypes_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => FormRecordTypes_1;
    get stringMap(): ObjectModel_1<Record<string, string>> {
        return this[_getPropertyModel_1]("stringMap", ObjectModel_1, [true]) as ObjectModel_1<Record<string, string>>;
    }
    get entityHierarchyMap(): ObjectModel_1<Record<string, FormEntityHierarchy_1>> {
        return this[_getPropertyModel_1]("entityHierarchyMap", ObjectModel_1, [true]) as ObjectModel_1<Record<string, FormEntityHierarchy_1>>;
    }
    get stringListMap(): ObjectModel_1<Record<string, ReadonlyArray<string>>> {
        return this[_getPropertyModel_1]("stringListMap", ObjectModel_1, [true]) as ObjectModel_1<Record<string, ReadonlyArray<string>>>;
    }
    get selfReferenceMap(): ObjectModel_1<Record<string, FormRecordTypes_1>> {
        return this[_getPropertyModel_1]("selfReferenceMap", ObjectModel_1, [true]) as ObjectModel_1<Record<string, FormRecordTypes_1>>;
    }
    get complexMap(): ObjectModel_1<Record<string, Record<string, ReadonlyArray<FormOptionalTypes_1>>>> {
        return this[_getPropertyModel_1]("complexMap", ObjectModel_1, [true]) as ObjectModel_1<Record<string, Record<string, ReadonlyArray<FormOptionalTypes_1>>>>;
    }
    get objectMap(): ObjectModel_1<Record<string, unknown>> {
        return this[_getPropertyModel_1]("objectMap", ObjectModel_1, [true]) as ObjectModel_1<Record<string, unknown>>;
    }
}
export default FormRecordTypesModel;
