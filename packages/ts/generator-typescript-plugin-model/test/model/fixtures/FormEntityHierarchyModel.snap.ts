import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormEntityHierarchy_1 from "./FormEntityHierarchy.js";
import FormEntityIdModel_1 from "./FormEntityIdModel.js";
class FormEntityHierarchyModel<T extends FormEntityHierarchy_1 = FormEntityHierarchy_1> extends FormEntityIdModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormEntityHierarchyModel);
    get lorem(): StringModel_1 {
        return this[_getPropertyModel_1]("lorem", (parent, key) => new StringModel_1(parent, key, false));
    }
    get ipsum(): NumberModel_1 {
        return this[_getPropertyModel_1]("ipsum", (parent, key) => new NumberModel_1(parent, key, false));
    }
}
export default FormEntityHierarchyModel;
