import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type SubPackageDependency_1 from "./SubPackageDependency.js";
class SubPackageDependencyModel<T extends SubPackageDependency_1 = SubPackageDependency_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SubPackageDependencyModel);
    get defaultField(): StringModel_1 {
        return this[_getPropertyModel_1]("defaultField", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SubPackageDependencyModel;
