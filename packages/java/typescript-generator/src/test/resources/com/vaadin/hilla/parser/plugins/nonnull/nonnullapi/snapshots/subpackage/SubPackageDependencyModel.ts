import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type SubPackageDependency from "./SubPackageDependency.js";
class SubPackageDependencyModel<T extends SubPackageDependency = SubPackageDependency> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(SubPackageDependencyModel);
    get defaultField(): StringModel {
        return this[_getPropertyModel]("defaultField", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SubPackageDependencyModel;
