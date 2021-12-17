import { _getPropertyModel as _getPropertyModel_1, NumberModel as NumberModel_1, StringModel as StringModel_1 } from "@vaadin/form";
import type FormEntityHierarchy_1 from "./FormEntityHierarchy";
import FormEntityIdModel_1 from "./FormEntityIdModel";
export default class FormEntityHierarchyModel<T extends FormEntityHierarchy_1 = FormEntityHierarchy_1> extends FormEntityIdModel_1<T> {
    static createEmptyValue: () => FormEntityHierarchy_1;
    get lorem(): StringModel_1 {
        return this[_getPropertyModel_1]("lorem", StringModel_1, [true]) as StringModel_1;
    }
    get ipsum(): NumberModel_1 {
        return this[_getPropertyModel_1]("ipsum", NumberModel_1, [true]) as NumberModel_1;
    }
}
