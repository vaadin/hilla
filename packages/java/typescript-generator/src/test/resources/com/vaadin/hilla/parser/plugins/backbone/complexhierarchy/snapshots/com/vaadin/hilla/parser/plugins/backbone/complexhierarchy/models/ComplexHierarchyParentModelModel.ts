import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import ComplexHierarchyGrandParentModelModel_1 from "./ComplexHierarchyGrandParentModelModel.js";
import type ComplexHierarchyParentModel_1 from "./ComplexHierarchyParentModel.js";
class ComplexHierarchyParentModelModel<T extends ComplexHierarchyParentModel_1 = ComplexHierarchyParentModel_1> extends ComplexHierarchyGrandParentModelModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ComplexHierarchyParentModelModel);
    get id(): StringModel_1 {
        return this[_getPropertyModel_1]("id", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default ComplexHierarchyParentModelModel;
