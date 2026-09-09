import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import ComplexHierarchyGrandParentModelModel from "./ComplexHierarchyGrandParentModelModel.js";
import type ComplexHierarchyParentModel from "./ComplexHierarchyParentModel.js";
class ComplexHierarchyParentModelModel<T extends ComplexHierarchyParentModel = ComplexHierarchyParentModel> extends ComplexHierarchyGrandParentModelModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ComplexHierarchyParentModelModel);
    get id(): StringModel {
        return this[_getPropertyModel]("id", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default ComplexHierarchyParentModelModel;
