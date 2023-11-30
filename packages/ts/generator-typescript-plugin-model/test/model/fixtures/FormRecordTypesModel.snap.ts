import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import type FormEntityHierarchy_1 from "./FormEntityHierarchy.js";
import type FormOptionalTypes_1 from "./FormOptionalTypes.js";
import type FormRecordTypes_1 from "./FormRecordTypes.js";
class FormRecordTypesModel<T extends FormRecordTypes_1 = FormRecordTypes_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormRecordTypesModel);
    get stringMap(): ObjectModel_1<Record<string, string | undefined>> {
        return this[_getPropertyModel_1]("stringMap", (parent, key) => new ObjectModel_1(parent, key, false));
    }
    get entityHierarchyMap(): ObjectModel_1<Record<string, FormEntityHierarchy_1 | undefined>> {
        return this[_getPropertyModel_1]("entityHierarchyMap", (parent, key) => new ObjectModel_1(parent, key, false));
    }
    get stringListMap(): ObjectModel_1<Record<string, ReadonlyArray<string | undefined> | undefined>> {
        return this[_getPropertyModel_1]("stringListMap", (parent, key) => new ObjectModel_1(parent, key, false));
    }
    get selfReferenceMap(): ObjectModel_1<Record<string, FormRecordTypes_1 | undefined>> {
        return this[_getPropertyModel_1]("selfReferenceMap", (parent, key) => new ObjectModel_1(parent, key, false));
    }
    get complexMap(): ObjectModel_1<Record<string, Record<string, ReadonlyArray<FormOptionalTypes_1 | undefined> | undefined> | undefined>> {
        return this[_getPropertyModel_1]("complexMap", (parent, key) => new ObjectModel_1(parent, key, false));
    }
    get objectMap(): ObjectModel_1<Record<string, unknown | undefined>> {
        return this[_getPropertyModel_1]("objectMap", (parent, key) => new ObjectModel_1(parent, key, false));
    }
}
export default FormRecordTypesModel;
