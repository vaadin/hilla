import { _getPropertyModel as _getPropertyModel_1, NumberModel as NumberModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormEntityHierarchy_1 from "./FormEntityHierarchy.js";
import FormEntityIdModel_1 from "./FormEntityIdModel.js";
class FormEntityHierarchyModel<T extends FormEntityHierarchy_1 = FormEntityHierarchy_1> extends FormEntityIdModel_1<T> {
    declare static createEmptyValue: () => FormEntityHierarchy_1;
    get lorem(): StringModel_1 {
        return this[_getPropertyModel_1]("lorem", StringModel_1, [false]) as StringModel_1;
    }
    get ipsum(): NumberModel_1 {
        return this[_getPropertyModel_1]("ipsum", NumberModel_1, [false]) as NumberModel_1;
    }
}
export default FormEntityHierarchyModel;
