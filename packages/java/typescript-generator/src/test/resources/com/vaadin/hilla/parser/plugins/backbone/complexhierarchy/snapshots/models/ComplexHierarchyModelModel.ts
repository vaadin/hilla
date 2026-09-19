import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import type ComplexHierarchyModel from "./ComplexHierarchyModel.js";
import ComplexHierarchyParentModelModel from "./ComplexHierarchyParentModelModel.js";
class ComplexHierarchyModelModel<T extends ComplexHierarchyModel = ComplexHierarchyModel> extends ComplexHierarchyParentModelModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ComplexHierarchyModelModel);
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default ComplexHierarchyModelModel;
