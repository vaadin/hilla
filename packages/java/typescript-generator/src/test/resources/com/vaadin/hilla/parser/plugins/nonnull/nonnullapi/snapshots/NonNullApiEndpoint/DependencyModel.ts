import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Dependency from "./Dependency.js";
class DependencyModel<T extends Dependency = Dependency> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(DependencyModel);
    get defaultField(): StringModel {
        return this[_getPropertyModel]("defaultField", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get nullableField(): StringModel {
        return this[_getPropertyModel]("nullableField", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get nullableSignatureField(): StringModel {
        return this[_getPropertyModel]("nullableSignatureField", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default DependencyModel;
