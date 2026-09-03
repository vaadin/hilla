import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type ComplexHierarchyModel_1 from "./ComplexHierarchyModel.js";
import ComplexHierarchyParentModelModel_1 from "./ComplexHierarchyParentModelModel.js";
class ComplexHierarchyModelModel<T extends ComplexHierarchyModel_1 = ComplexHierarchyModel_1> extends ComplexHierarchyParentModelModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ComplexHierarchyModelModel);
    get name(): StringModel_1 {
        return this[_getPropertyModel_1]("name", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default ComplexHierarchyModelModel;
