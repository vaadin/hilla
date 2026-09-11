import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type FormEntityHierarchy from "./FormEntityHierarchy.js";
import type FormOptionalTypes from "./FormOptionalTypes.js";
import type FormRecordTypes from "./FormRecordTypes.js";
class FormRecordTypesModel<T extends FormRecordTypes = FormRecordTypes> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormRecordTypesModel);
    get stringMap(): ObjectModel<Record<string, string | undefined>> {
        return this[_getPropertyModel]("stringMap", (parent, key) => new ObjectModel(parent, key, false));
    }
    get entityHierarchyMap(): ObjectModel<Record<string, FormEntityHierarchy | undefined>> {
        return this[_getPropertyModel]("entityHierarchyMap", (parent, key) => new ObjectModel(parent, key, false));
    }
    get stringListMap(): ObjectModel<Record<string, ReadonlyArray<string | undefined> | undefined>> {
        return this[_getPropertyModel]("stringListMap", (parent, key) => new ObjectModel(parent, key, false));
    }
    get selfReferenceMap(): ObjectModel<Record<string, FormRecordTypes | undefined>> {
        return this[_getPropertyModel]("selfReferenceMap", (parent, key) => new ObjectModel(parent, key, false));
    }
    get complexMap(): ObjectModel<Record<string, Record<string, ReadonlyArray<FormOptionalTypes | undefined> | undefined> | undefined>> {
        return this[_getPropertyModel]("complexMap", (parent, key) => new ObjectModel(parent, key, false));
    }
    get objectMap(): ObjectModel<Record<string, unknown | undefined>> {
        return this[_getPropertyModel]("objectMap", (parent, key) => new ObjectModel(parent, key, false));
    }
}
export default FormRecordTypesModel;
