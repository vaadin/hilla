import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import type ComplexHierarchyGrandParentModel from "./ComplexHierarchyGrandParentModel.js";
class ComplexHierarchyGrandParentModelModel<T extends ComplexHierarchyGrandParentModel = ComplexHierarchyGrandParentModel> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ComplexHierarchyGrandParentModelModel);
    get build(): NumberModel {
        return this[_getPropertyModel]("build", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default ComplexHierarchyGrandParentModelModel;
