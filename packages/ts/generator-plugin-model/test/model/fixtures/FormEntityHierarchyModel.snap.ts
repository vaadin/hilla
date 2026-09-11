import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, StringModel } from "@vaadin/hilla-lit-form";
import type FormEntityHierarchy from "./FormEntityHierarchy.js";
import FormEntityIdModel from "./FormEntityIdModel.js";
class FormEntityHierarchyModel<T extends FormEntityHierarchy = FormEntityHierarchy> extends FormEntityIdModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormEntityHierarchyModel);
    get lorem(): StringModel {
        return this[_getPropertyModel]("lorem", (parent, key) => new StringModel(parent, key, false));
    }
    get ipsum(): NumberModel {
        return this[_getPropertyModel]("ipsum", (parent, key) => new NumberModel(parent, key, false));
    }
}
export default FormEntityHierarchyModel;
