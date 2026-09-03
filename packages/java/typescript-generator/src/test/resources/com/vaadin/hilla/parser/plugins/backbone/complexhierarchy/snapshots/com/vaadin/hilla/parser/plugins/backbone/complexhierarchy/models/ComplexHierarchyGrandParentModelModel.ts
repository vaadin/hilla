import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type ComplexHierarchyGrandParentModel_1 from "./ComplexHierarchyGrandParentModel.js";
class ComplexHierarchyGrandParentModelModel<T extends ComplexHierarchyGrandParentModel_1 = ComplexHierarchyGrandParentModel_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ComplexHierarchyGrandParentModelModel);
    get build(): NumberModel_1 {
        return this[_getPropertyModel_1]("build", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default ComplexHierarchyGrandParentModelModel;
